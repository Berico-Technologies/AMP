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
    public class StreamingCollectionRegistration<TEvent> : IRegistration 
    {
        protected IStreamingCollectionHandler<TEvent> _eventHandler;
        protected Predicate<Envelope> _filterPredicate;
        protected Func<Envelope, object> _processInbound;
        protected IDictionary<string, string> _registrationInfo;
        protected ILog _log;
        protected IDictionary<string, IDictionary<int, StreamingEventItem<TEvent>>> _collectedEvents;
        protected IDictionary<string, int> _collectionSizes;

        public StreamingCollectionRegistration(IStreamingCollectionHandler<TEvent> handler, Func<Envelope, object> processorCallback) 
        {
            _eventHandler = handler;
            _processInbound = processorCallback;
            _registrationInfo = new Dictionary<string, string>();
            _registrationInfo.SetMessageTopic(handler.EventType.FullName);

            _log = LogManager.GetLogger(this.GetType());
            _collectedEvents = new Dictionary<string, IDictionary<int, StreamingEventItem<TEvent>>>();
            _collectionSizes = new Dictionary<string, int>();
        }

        public Predicate<Envelope> Filter
        {
            get { return _filterPredicate; }
        }

        private static readonly object padLock = new object();

        public object Handle(Envelope env)
        {
            lock (padLock)
            {
                
                object result = null;

                try
                {
                    if (IsEndOfStream(env))
                    {
                        CloseStream(env);
                    }
                    else if (IsCollectionSizeNotification(env))
                    {
                        StoreExpectedCollectionSize(env);
                    }
                    else
                    {
                        QueueEvent(env);
                    }
                }
                catch (Exception ex)
                {
                    result = HandleFailed(env, ex);
                }
                return result;
            }
        }

        private void QueueEvent(Envelope env)
        {
            if (null != env)
            {
                string sequenceId = env.Headers[SEC.SEQUENCE_ID];
                TEvent evt = (TEvent)_processInbound( env);

                if (null != evt)
                {
                    if (!_collectedEvents.ContainsKey(sequenceId))
                    {
                        _collectedEvents.Add(sequenceId, new Dictionary<int, StreamingEventItem<TEvent>>());
                    }
                    StreamingEventItem<TEvent> eventItem = new StreamingEventItem<TEvent>(evt, env.Headers);
                    IDictionary<int, StreamingEventItem<TEvent>> eventMap = _collectedEvents[sequenceId];
                    eventMap.Add(eventItem.Position, eventItem);
                    UpdatePercentProcessed(sequenceId, eventMap.Count);
                    
                }
            }
        }

        private void UpdatePercentProcessed(string sequenceId, int numProcessed)
        {
            int collectionSize = _collectionSizes[sequenceId];
            double percentProcessed = 0.00;

            if (collectionSize > 0) 
            {
                percentProcessed = Math.Round(((double.Parse(numProcessed + "") / double.Parse(collectionSize + ""))), 2) * 100;
            }
            _eventHandler.OnPercentCollectionReceived(percentProcessed);
        }

        private void CloseStream(Envelope env)
        {
            EndOfStream eos = (EndOfStream)_processInbound( env);
            string sequenceId = eos.SequenceId;
            IDictionary<int, StreamingEventItem<TEvent>> events = _collectedEvents[sequenceId];
            IEnumerable<StreamingEventItem<TEvent>> sortedEvents = from pair in events 
                                                                   orderby pair.Key
                                                                   select pair.Value;
            _eventHandler.HandleCollection(sortedEvents);
            _collectedEvents.Remove(sequenceId);
            _collectionSizes.Remove(sequenceId);
        }

        private void StoreExpectedCollectionSize(Envelope env)
        {
            CollectionSizeNotifier collectionSizeNotifier = (CollectionSizeNotifier)_processInbound(env);
            _collectionSizes.Add(collectionSizeNotifier.SequenceId, collectionSizeNotifier.Size);
        }

        private bool IsCollectionSizeNotification(Envelope env)
        {
            string messageType = env.GetMessageType();
            if (messageType == typeof(CollectionSizeNotifier).FullName)
            {
                return true;
            }
            return false;
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
                _log.Error("Unable to process envelop wth message topic: " + env.GetMessageTopic() + " from stream.");
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
    }
}
