using cmf.bus;
using cmf.eventing;
using cmf.eventing.patterns.streaming;
using Common.Logging;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using SEC = cmf.eventing.patterns.streaming.StreamingEnvelopeConstants;

namespace amp.eventing.streaming
{
    public class StreamingReaderRegistration<TEvent> : IRegistration, IObservable<StreamingEventItem<TEvent>> 
    {
        protected ILog _log;
        protected IStreamingReaderHandler<TEvent> _eventHandler;
        protected Func<Envelope, object> _processorCallback;
        protected IDictionary<string, string> _registrationInfo;
        private IList<IObserver<StreamingEventItem<TEvent>>> _observers;

        public StreamingReaderRegistration(IStreamingReaderHandler<TEvent> eventHandler, Func<Envelope, object> processorCallback)
        {
            //IStreamingReaderHandler implements IObserver<IStreamingEventItem<TEvent>>
            _observers = new List<IObserver<StreamingEventItem<TEvent>>>();
            _observers.Add(eventHandler);

            _eventHandler = eventHandler;
            _processorCallback = processorCallback;

            _registrationInfo = new Dictionary<string, string>();
            _registrationInfo.Add(EnvelopeHeaderConstants.MESSAGE_TOPIC, _eventHandler.EventType.FullName);
            _log = LogManager.GetLogger(typeof(StreamingReaderRegistration<TEvent>).Name);
        }

        public Predicate<Envelope> Filter
        {
            get { return null; }
        }

        private static readonly object padLock = new object();

        public object Handle(Envelope env)
        {
            lock (padLock)
            {
                _log.Debug("enter Handle for StreamingReaderRegistration");
                object result = null;
                
                try
                {
                    if (IsEndOfStream(env))
                    {
                        foreach (IObserver<StreamingEventItem<TEvent>> o in _observers)
                        {
                            o.OnCompleted();
                        }
                        _eventHandler.Dispose();
                    }
                    else
                    {
                        TEvent evt = (TEvent)_processorCallback(env);
                        if (null != evt)
                        {
                            StreamingEventItem<TEvent> eventItem = new StreamingEventItem<TEvent>(evt, env.Headers);
                            foreach (IObserver<StreamingEventItem<TEvent>> o in _observers)
                            {
                                o.OnNext(eventItem);
                            }
                        }
                    }
                }
                catch (Exception ex)
                {
                    result = HandleFailed(env, ex);
                }
                
                _log.Debug("leaving Handle for StreamingReaderRegistration");
                return result;
            }
        }

        private bool IsEndOfStream(Envelope env)
        {
            string messageType = env.GetMessageType();
            if (messageType == typeof(EndOfStream).FullName)
            {
                return true;
            }
            return false;
        }

        public object HandleFailed(Envelope env, Exception ex)
        {
            try
            {
                _log.Error("Unable to process envelope with message topic: " + env.GetMessageTopic() + " from stream.", ex);
                return null;
            }
            catch (Exception failedToFail)
            {
                throw failedToFail;
            }
        }

        public IDictionary<string, string> Info
        {
            get { return _registrationInfo; }
        }

        public IDisposable Subscribe(IObserver<StreamingEventItem<TEvent>> observer)
        {
            if (false == _observers.Contains(observer))
            {
                _observers.Add(observer);
            }
            return new StreamingReaderUnsubscriber<TEvent>(_observers, observer);
        }

        private class StreamingReaderUnsubscriber<TEvent>: IDisposable 
        { 
            /// <summary>
            /// A reference to a list of observers being maintained by implementation of IObservable.
            /// </summary>
            private IList<IObserver<StreamingEventItem<TEvent>>> _observers;
            private IObserver<StreamingEventItem<TEvent>> _observer;

            public StreamingReaderUnsubscriber(IList<IObserver<StreamingEventItem<TEvent>>> observers,
                IObserver<StreamingEventItem<TEvent>> observer)
            {
                _observers = observers;
                _observer = observer;
            }

            public void Dispose()
            {
                if (_observer != null && _observers.Contains(_observer))
                {
                    _observers.Remove(_observer);
                }
            }
        
        }
    }
}
