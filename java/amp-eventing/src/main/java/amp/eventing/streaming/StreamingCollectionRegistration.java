package amp.eventing.streaming;

import amp.eventing.EnvelopeHelper;
import amp.eventing.EventContext;
import amp.eventing.IInboundProcessorCallback;
import cmf.bus.Envelope;
import cmf.bus.EnvelopeHeaderConstants;
import cmf.bus.IEnvelopeFilterPredicate;
import cmf.bus.IRegistration;
import cmf.eventing.patterns.streaming.IStreamingCollectionHandler;
import cmf.eventing.patterns.streaming.StreamingEventItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static cmf.eventing.patterns.streaming.StreamingEnvelopeConstants.*;
import static java.util.Arrays.sort;

/**
 * Specialized {@link cmf.bus.IRegistration} that handles the event by aggregating events from a common sequence
 * and publishing them to a {@link java.util.Collection}.
 * User: jholmberg
 * Date: 6/5/13
 */
public class StreamingCollectionRegistration<TEVENT> implements IRegistration {
    protected static final Logger log = LoggerFactory.getLogger(DefaultStreamingBus.class);
    protected IStreamingCollectionHandler<TEVENT> eventHandler;
    protected IEnvelopeFilterPredicate filterPredicate;
    protected IInboundProcessorCallback processorCallback;
    protected Map<String, String> registrationInfo;
    protected ConcurrentHashMap<String, Map<Integer,StreamingEventItem<TEVENT>>> collectedEvents;

    @Override
    public IEnvelopeFilterPredicate getFilterPredicate() {
        return filterPredicate;
    }

    @Override
    public Map<String, String> getRegistrationInfo() {
        return registrationInfo;
    }

    public StreamingCollectionRegistration(IStreamingCollectionHandler<TEVENT> handler, IInboundProcessorCallback processorCallback) {
        this.eventHandler = handler;
        this.processorCallback = processorCallback;

        registrationInfo = new HashMap<String, String>();
        registrationInfo.put(EnvelopeHeaderConstants.MESSAGE_TOPIC, eventHandler.getEventType().getCanonicalName());
        this.collectedEvents = new ConcurrentHashMap<String, Map<Integer, StreamingEventItem<TEVENT>>>();
    }

    @Override
    public Object handle(Envelope env) throws Exception {
        Object result = null;

        try {
            if (isEndOfStream(env)) {
                closeStream(env);
            }  else {
                queueEvent(env);
            }
        } catch (Exception ex) {
            result = handleFailed(env, ex);
        }
        return result;
    }

    private void queueEvent(Envelope env) throws Exception {
        if (null != env) {
            String sequenceId = env.getHeader(SEQUENCE_ID);
            TEVENT event = (TEVENT) this.processorCallback.ProcessInbound(env);
            if (null != event) {
                if (!collectedEvents.containsKey(sequenceId)) {
                    collectedEvents.put(sequenceId, new HashMap<Integer,StreamingEventItem<TEVENT>>());
                }
                StreamingEventItem<TEVENT> eventItem = new StreamingEventItem(event, env.getHeaders());
                Map<Integer, StreamingEventItem<TEVENT>> eventMap = collectedEvents.get(sequenceId);
                eventMap.put(eventItem.getPosition(), eventItem);
                eventHandler.onIncrement(eventMap.size());
            }
        }
    }

    private void closeStream(Envelope env) throws Exception {
        EndOfStream eos = (EndOfStream) this.processorCallback.ProcessInbound(env);
        String sequenceId = eos.getSequenceId();
        Map<Integer, StreamingEventItem<TEVENT>> events = collectedEvents.get(sequenceId);
        Collection<StreamingEventItem<TEVENT>> sortedEvents = sortEventsByPosition(events);

        this.eventHandler.handleCollection(sortedEvents);
        collectedEvents.remove(sequenceId);
    }

    private boolean isEndOfStream(Envelope env) {
        EnvelopeHelper envelope = new EnvelopeHelper(env);
        String messageType = envelope.getMessageType();
        if (messageType.equals(EndOfStream.class.getCanonicalName())) {
            return true;
        }
        return false;
    }

    private Collection<StreamingEventItem<TEVENT>> sortEventsByPosition(Map<Integer, StreamingEventItem<TEVENT>> events) {
        Set<Integer> keys = events.keySet();
        List<Integer> positions = new ArrayList<Integer>();
        for (int key : keys) {
            positions.add(key);
        }
        Collections.sort(positions);
        Collection<StreamingEventItem<TEVENT>> sortedEvents = new ArrayList<StreamingEventItem<TEVENT>>();
        for (int position : positions) {
            sortedEvents.add(events.get(position));
        }

        return sortedEvents;
    }

    @Override
    public Object handleFailed(Envelope env, Exception ex) throws Exception {
        try {
            EnvelopeHelper envelope = new EnvelopeHelper(env);
            log.error("Unable to process envelope with message topic: " + envelope.getMessageTopic() + " from stream.", ex);
            return null;
        } catch (Exception failedToFail) {
            throw failedToFail;
        }
    }
}
