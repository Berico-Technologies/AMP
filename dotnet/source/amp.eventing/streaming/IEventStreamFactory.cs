using cmf.eventing.patterns.streaming;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace amp.eventing.streaming
{
    public interface IEventStreamFactory
    {
        IStandardStreamingEventBus EventBus { set; }
        
        string Topic { set; }

        IEventStream GenerateEventStream();
    }
}
