package amp.eventing.streaming;

import amp.eventing.EventContext;
import amp.eventing.IContinuationCallback;
import amp.eventing.EnvelopeHelper;
import cmf.bus.Envelope;
import cmf.eventing.IEventBus;
import cmf.eventing.patterns.streaming.IEventStream;
import cmf.eventing.patterns.streaming.IStreamingEventItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DefaultEventStream implements IEventStream {
    protected static final Logger log = LoggerFactory.getLogger(DefaultEventStream.class);
    private final DefaultStreamingBus eventBus;
    private int batchLimit = 2;
    private Queue<EventStreamQueueItem> queuedEvents;
    private final UUID sequenceId;
    private int position;
    private String topic;

    public DefaultEventStream(DefaultStreamingBus eventBus, String topic) {
        this.eventBus = eventBus;
        this.topic = topic;
        this.queuedEvents = new LinkedList<EventStreamQueueItem>();
        this.sequenceId = UUID.randomUUID();
        this.position = 0;
    }

    @Override
    public void setBatchLimit(int numberOfEvents) {
        this.batchLimit = numberOfEvents;
    }

    @Override
    public void publish(Object event) throws Exception {
        log.debug("enter publish to stream");
        boolean isLast = false;
        String sequence = sequenceId.toString();
        String isLastFlag = Boolean.toString(isLast);

        Envelope env = StreamingEnvelopeHelper.buildStreamingEnvelope(sequence, position, isLastFlag);
        EnvelopeHelper envelopHelper = new EnvelopeHelper(env);
        envelopHelper.setMessageTopic(getTopic());

        EventContext context = new EventContext(EventContext.Directions.Out, env, event);
        EventStreamQueueItem eventItem = new EventStreamQueueItem(context);

        log.debug("buffering event with sequenceId: " + sequence + ", position: " + position + ", isLast: " + isLastFlag);
        this.queuedEvents.add(eventItem);

        if (this.queuedEvents.size() == (this.batchLimit + 1)) {
            log.debug("flushing " + batchLimit + " event(s) to stream.");
            boolean isComplete = false;
            flushStreamBuffer(isComplete);
        }

        position++;
    }

    @Override
    public String getTopic() {
        return this.topic;
    }

    /**
     * When processing a stream of an unknown size, it becomes a challenge to know when you have dealt with the
     * last object in that stream. This class utilizes the dispose() method to indicate that stream processing
     * has completed. This is necessary in order to mark the last message with the isLast flag
     * set to true. The trick here is to ensure that the streamBuffer is not entirely empty when
     * dispose gets called.
     */
    private void flushStreamBuffer(boolean isComplete) throws Exception {
        int boundary = (isComplete) ? 0 : 1;
        //We'll flush out the batch of messages == batchLimit and leave one left in the queue to either
        //be sent with the next batch or to be sent when dispose gets called.
        while (queuedEvents.size() > boundary) {
            final EventStreamQueueItem eventItem = queuedEvents.remove();
            eventBus.processEvent(eventItem.getEventContext(),
                    eventBus.getOutboundProcessors(),
                    new IContinuationCallback() {
                        @Override
                        public void continueProcessing() throws Exception {
                            eventBus.getEnvelopeBus().send(eventItem.getEnvelope());
                        }
                    });
        }
    }

    /**
     * Flushes remaining items in the buffer. This MUST be called to close out the stream or
     * the subscriber will never be notified with the last item in the sequence.
     */
    @Override
    public void dispose() {
        boolean isComplete = true;
        try {
            markLastElementInQueue();
            flushStreamBuffer(isComplete);
        } catch (Exception e) {
            log.error("Unable to send last batch of messages in buffer to event stream.", e);
        }
    }

    /**
     * Ensures that the last element in the queue has the isLast header set to true marking the end of the stream.
     */
    private void markLastElementInQueue() {
        int counter = 0;
        for (EventStreamQueueItem item : queuedEvents) {
            if (counter == (queuedEvents.size() - 1)) {
                item.getEnvelope().setHeader(StreamingEnvelopeConstants.IS_LAST, Boolean.toString(true));
            }
            counter++;
        }
    }
}
