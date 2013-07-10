using cmf.bus;
using cmf.eventing;
using cmf.eventing.patterns.streaming;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace amp.eventing.streaming
{
    public interface IStandardStreamingEventBus : IStreamingEventBus 
    {
        IList<IEventProcessor> InboundProcessors { get; }
        IList<IEventProcessor> OutboundProcessors { get; }
        IEnvelopeBus EnvelopeBus { get; }
        void ProcessEvent(EventContext context, IEnumerable<IEventProcessor> processorChain, Action processingComplete);
    }
}
