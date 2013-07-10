package amp.eventing;


import java.util.*;

import cmf.bus.Envelope;
import cmf.bus.IEnvelopeBus;
import cmf.eventing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import amp.eventing.EventContext.Directions;


public class DefaultEventBus implements IEventBus, IInboundProcessorCallback {

    protected static final Logger log = LoggerFactory.getLogger(DefaultEventBus.class);
    protected IEnvelopeBus envelopeBus;
    protected List<IEventProcessor> inboundProcessors = new LinkedList<IEventProcessor>();
    protected List<IEventProcessor> outboundProcessors = new LinkedList<IEventProcessor>();




    public DefaultEventBus(IEnvelopeBus envelopeBus) {
        this.envelopeBus = envelopeBus;
        inboundProcessors = new ArrayList<IEventProcessor>();
        outboundProcessors = new ArrayList<IEventProcessor>();
    }

    public DefaultEventBus(IEnvelopeBus envelopeBus, List<IEventProcessor> inboundProcessors,
                           List<IEventProcessor> outboundProcessors) {
        this.envelopeBus = envelopeBus;
        this.inboundProcessors = inboundProcessors;
        this.outboundProcessors = outboundProcessors;
    }


    @Override
    public void dispose() {
        envelopeBus.dispose();
    }

    public void processEvent(
            final EventContext context,
            final List<IEventProcessor> processingChain,
            final IContinuationCallback onComplete) throws Exception {
        log.debug("Enter processEvent");

        // if the chain is null or empty, complete processing
        if ((null == processingChain) || (0 == processingChain.size())) {
            log.debug("event processing complete");
            onComplete.continueProcessing();
            return;
        }

        // get the first processor
        IEventProcessor processor = processingChain.get(0);

        // create a processing chain that no longer contains this processor
        final List<IEventProcessor> newChain = processingChain.subList(1, processingChain.size());

        // let it process the event and pass its "next" processor: a method that
        // recursively calls this function with the current processor removed
        processor.processEvent(context, new IContinuationCallback() {

            @Override
            public void continueProcessing() throws Exception {
                processEvent(context, newChain, onComplete);
            }

        });

        log.debug("Leave processEvent");
    }

    @Override
    public void publish(Object event) throws Exception {

        log.debug("enter publish");

        if (event == null) {
            throw new IllegalArgumentException("Cannot publish a null event");
        }

        final EventContext context = new EventContext(Directions.Out, new Envelope(), event);

        this.processEvent(
                context,
                outboundProcessors,
                new IContinuationCallback() {
                    @Override
                    public void continueProcessing() throws Exception {
                        envelopeBus.send(context.getEnvelope());
                    }
                });

        log.debug("leave publish");
    }

    @Override
    public <TEVENT> void subscribe(IEventHandler<TEVENT> eventHandler) throws Exception {
        Class<TEVENT> type = eventHandler.getEventType();
        IEventFilterPredicate filterPredicate = new TypeEventFilterPredicate(type);
        subscribe(eventHandler, filterPredicate);
    }

    @Override
    public <TEVENT> void subscribe(final IEventHandler<TEVENT> eventHandler, final IEventFilterPredicate filterPredicate)
            throws Exception {
        EventRegistration registration = new EventRegistration(eventHandler, this);
        envelopeBus.register(registration);
    }

    @Override
    public Object ProcessInbound(Envelope envelope) throws Exception {
        final EventContext context = new EventContext(Directions.In, envelope);

        this.processEvent(
                context,
                this.inboundProcessors,
                new IContinuationCallback() {

                    @Override
                    public void continueProcessing() {
                        log.info("Completed inbound processing - returning event");
                    }
                });

        return context.getEvent();
    }

    @Override
    protected void finalize() {
        dispose();
    }


}
