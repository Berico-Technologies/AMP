using cmf.eventing.patterns.streaming;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace amp.eventing.streaming
{
    public class DefaultEventStreamFactory : IEventStreamFactory 
    {
        private IStandardStreamingEventBus _eventBus;
        private string _topic;

        public string Topic { set { _topic = value; } }

        public IEventStream GenerateEventStream()
        {
            return new DefaultEventStream(_eventBus, _topic);
        }

        public IStandardStreamingEventBus EventBus
        {
            set { _eventBus = value; }
        }
    }
}
