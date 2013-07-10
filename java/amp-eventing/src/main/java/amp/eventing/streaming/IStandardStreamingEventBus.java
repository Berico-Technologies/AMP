package amp.eventing.streaming;

import amp.eventing.EventContext;
import amp.eventing.IContinuationCallback;
import amp.eventing.IEventProcessor;
import cmf.bus.IEnvelopeBus;
import cmf.eventing.patterns.streaming.IStreamingEventBus;

import java.util.List;

public interface IStandardStreamingEventBus extends IStreamingEventBus {
    public List<IEventProcessor> getInboundProcessors();

    public List<IEventProcessor> getOutboundProcessors();

    public IEnvelopeBus getEnvelopeBus();

    void processEvent(final EventContext context, final List<IEventProcessor> processingChain, final IContinuationCallback onComplete) throws Exception;
}
