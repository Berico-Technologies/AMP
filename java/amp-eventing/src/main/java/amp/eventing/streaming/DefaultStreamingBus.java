package amp.eventing.streaming;


import amp.eventing.*;
import cmf.bus.IEnvelopeBus;
import cmf.eventing.IEventFilterPredicate;
import cmf.eventing.IEventHandler;
import cmf.eventing.patterns.streaming.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DefaultStreamingBus extends DefaultEventBus implements IStandardStreamingEventBus, IInboundProcessorCallback {
    protected static final Logger log = LoggerFactory.getLogger(DefaultStreamingBus.class);
    private IEventStreamFactory eventStreamFactory;
    private Map<String, IEventStream> eventStreams;
    /**
     * Default setting for {@link cmf.eventing.patterns.streaming.IStreamingEventBus} to stream events in batches.
     * Allows for tuning by setting this to a different value based on size of events.
     */
    protected int batchLimit = 10;

    public DefaultStreamingBus(IEnvelopeBus envelopeBus) {
        super(envelopeBus);
        initializeDefaults();
    }

    public DefaultStreamingBus(IEnvelopeBus envelopeBus, List<IEventProcessor> inboundProcessors,
                               List<IEventProcessor> outboundProcessors) {
        super(envelopeBus, inboundProcessors, outboundProcessors);
        initializeDefaults();
    }


    public DefaultStreamingBus(IEnvelopeBus envelopeBus, List<IEventProcessor> inboundProcessors,
                               List<IEventProcessor> outboundProcessors,
                               IEventStreamFactory eventStreamFactory) {
        super(envelopeBus, inboundProcessors, outboundProcessors);
        this.eventStreamFactory = eventStreamFactory;
        this.eventStreams = new HashMap<String, IEventStream>();
    }

    private void initializeDefaults() {
        this.eventStreamFactory = new DefaultEventStreamFactory();
        this.eventStreamFactory.setEventBus(this);
        this.eventStreams = new HashMap<String, IEventStream>();
    }

    @Override
    public IEventStream createStream(String topic) {
        if (false == this.eventStreams.containsKey(topic)) {
            eventStreamFactory.setTopic(topic);
            IEventStream eventStream = eventStreamFactory.generateEventStream();
            this.eventStreams.put(topic, eventStream);
            return eventStream;
        } else {
            return this.eventStreams.get(topic);
        }

    }

    public void removeStream(String topic) {
        if (this.eventStreams.containsKey(topic)) {
            this.eventStreams.remove(topic);
        }
    }

    @Override
    public <TEVENT> void publishChunkedSequence(Collection<TEVENT> dataSet) throws Exception {
        log.debug("enter publish to chunked sequence");
        IEventStream eventStream = null;
        String topic = null;

        validateEventCollection(dataSet);

        try {

            for (TEVENT eventItem : dataSet) {
                if (null == eventStream) {
                    topic = eventItem.getClass().getCanonicalName();
                    eventStream = new DefaultEventStream(this, topic); //Skipping use of the factory so that we ensure sequencing based event stream is used
                    eventStream.setBatchLimit(this.batchLimit);
                    //Notify the receiver what the size of the collection will be
                    this.publish(new CollectionSizeNotifier(dataSet.size(), topic, eventStream.getSequenceId()));
                    eventStreams.put(topic, eventStream);
                }

                eventStream.publish(eventItem);
            }
        } finally {
            if (null != eventStream) {
                eventStream.dispose();
                removeStream(topic);
            }
        }
        log.debug("leave publish to chunked sequence");
    }


    private <TEVENT> void validateEventCollection(Collection<TEVENT> eventIterator) throws Exception {
        if (null == eventIterator) {
            throw new IllegalArgumentException("Cannot publish a null event iterator");
        }
    }

    @Override
    public <TEVENT> void subscribeToCollection(IStreamingCollectionHandler<TEVENT> handler) throws Exception {
        log.debug("enter subscribeToCollection");
        StreamingCollectionRegistration<TEVENT> registration = new StreamingCollectionRegistration<TEVENT>(handler, this);
        envelopeBus.register(registration);
        log.debug("leave subscribeToCollection");
    }

    @Override
    public <TEVENT> void subscribeToReader(IStreamingReaderHandler<TEVENT> handler) throws Exception {
        log.debug("enter subscribeToReader");
        StreamingReaderRegistration<TEVENT> registration = new StreamingReaderRegistration<TEVENT>(handler, this);
        envelopeBus.register(registration);
        log.debug("leave subscribeToReader");
    }

    @Override
    public <TEVENT> void subscribe(final IEventHandler<TEVENT> eventHandler, final IEventFilterPredicate filterPredicate)
            throws Exception {
        log.debug("enter default streaming bus subscribe.");

    }

    @Override
    public List<IEventProcessor> getInboundProcessors() {
        return this.inboundProcessors;
    }

    @Override
    public List<IEventProcessor> getOutboundProcessors() {
        return this.outboundProcessors;
    }

    @Override
    public IEnvelopeBus getEnvelopeBus() {
        return this.envelopeBus;
    }

}
