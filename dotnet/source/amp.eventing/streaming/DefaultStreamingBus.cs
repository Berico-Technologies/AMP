using cmf.bus;
using cmf.eventing;
using cmf.eventing.patterns.streaming;
using Common.Logging;
using System;
using System.Collections.Generic;

namespace amp.eventing.streaming
{
    public class DefaultStreamingBus : DefaultEventBus, IStandardStreamingEventBus
    {
        protected ILog _log;
        private IEventStreamFactory _eventStreamFactory;
        private Dictionary<string, IEventStream> _eventStreams;
        
        protected int _batchLimit = 10;

        public DefaultStreamingBus(IEnvelopeBus envelopeBus) : base(envelopeBus)
        {
            _log = LogManager.GetLogger(this.GetType());
            InboundChain = new Dictionary<int, IEventProcessor>();
            OutboundChain = new Dictionary<int, IEventProcessor>();

            InitializeDefaults();
        }

        public DefaultStreamingBus(IEnvelopeBus envelopeBus, 
                                    IEventStreamFactory eventStreamFactory) : base(envelopeBus)
        {
            _log = LogManager.GetLogger(this.GetType());
            _eventStreamFactory = eventStreamFactory;
            _eventStreams = new Dictionary<string, IEventStream>();
           
        }

        private void InitializeDefaults()
        {
            _eventStreamFactory = new DefaultEventStreamFactory();
            _eventStreamFactory.EventBus = this;
            _eventStreams = new Dictionary<string,IEventStream>();
        }

        public IEventStream CreateStream(string topic)
        {
            if (false == _eventStreams.ContainsKey(topic))
            {
                _eventStreamFactory.Topic = topic;
                IEventStream eventStream = _eventStreamFactory.GenerateEventStream();
                _eventStreams.Add(topic, eventStream);
                return eventStream;
            }
            else
            {
                return _eventStreams[topic];
            }
        }

        public void RemoveStream(string topic) 
        {
            if (_eventStreams.ContainsKey(topic))
            {
                _eventStreams.Remove(topic);
            }
        }

        public void PublishChunkedSequence<TEvent>(ICollection<TEvent> dataSet)
        {
            _log.Debug("enter publish to chunked sequence");
            IEventStream eventStream = null;
            string topic = null;

            ValidateEventCollection(dataSet);
            
            try
            {
                foreach (TEvent eventItem in dataSet)
                {
                    if (null == eventStream)
                    {
                        topic = eventItem.GetType().ToString();
                        eventStream = new DefaultEventStream(this, topic); //Skipping use of the factory so that we ensure sequencing based event stream is used
                        eventStream.BatchLimit = _batchLimit;
                        //Notify the receiver what the size of the collection will be
                        Publish(new CollectionSizeNotifier(dataSet.Count, topic, eventStream.SequenceId));
                        _eventStreams.Add(topic, eventStream);
                    }

                    eventStream.Publish(eventItem);
                }
            }
            finally
            {
                if (null != eventStream)
                {
                    eventStream.Dispose();
                    RemoveStream(topic);
                }
            }
            _log.Debug("leave publish to chunked sequence");
        }

        private void ValidateEventCollection<TEvent>(ICollection<TEvent> eventEnumerator)
        {
            if (null == eventEnumerator)
            {
                throw new ArgumentException("Cannot publish from a null event iterator.");
            }
        }

        public void SubscribeToCollection<TEvent>(IStreamingCollectionHandler<TEvent> handler)
        {
            _log.Debug("enter SubscribeToCollection");
            StreamingCollectionRegistration<TEvent> registration = new StreamingCollectionRegistration<TEvent>(handler, this.ProcessInboundCallback);
            _envBus.Register(registration);
            _log.Debug("leave SubscribetoCollection");
        }

        public void SubscribeToReader<TEvent>(IStreamingReaderHandler<TEvent> handler)
        {
            _log.Debug("enter SubscribeToReader");
            StreamingReaderRegistration<TEvent> registration = new StreamingReaderRegistration<TEvent>(handler, this.ProcessInboundCallback);
            _envBus.Register(registration);
            _log.Debug("leave SubscribeToReader");
        }

        
        public IList<IEventProcessor> InboundProcessors
        {
            get
            {
                return new List<IEventProcessor>(InboundChain.Sort());
            }
        }

        public IList<IEventProcessor> OutboundProcessors
        {
            get
            {
                return new List<IEventProcessor>(OutboundChain.Sort());
            }
        }

        public IEnvelopeBus EnvelopeBus
        {
            get
            {
                return _envBus;
            }
        }

        public object ProcessInboundCallback(Envelope env)
        {
            _log.Debug("Enter InterceptEvent");

            var context = new EventContext(EventContext.Directions.In, env);

            this.ProcessEvent(context, this.InboundChain.Sort(), () =>
            {
                _log.Info("Completed inbound processing - invoking IEventHandler");

            });

            _log.Debug("Leave InterceptEvent");
            return context.Event;
        }
        
    }
}