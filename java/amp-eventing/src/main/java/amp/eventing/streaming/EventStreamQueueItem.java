package amp.eventing.streaming;

import amp.eventing.EventContext;
import cmf.bus.Envelope;

/**
 * Contains the elements needed to send an event in an event stream
 * User: jholmberg
 * Date: 6/3/13
 */
public class EventStreamQueueItem {

    private final EventContext eventContext;

    public EventStreamQueueItem(EventContext eventContext) {
        this.eventContext = eventContext;
    }

    public Envelope getEnvelope() {
        return this.eventContext.getEnvelope();
    }

    public EventContext getEventContext() {
        return this.eventContext;
    }
}
