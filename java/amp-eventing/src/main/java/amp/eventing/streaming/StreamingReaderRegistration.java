package amp.eventing.streaming;

import amp.messaging.EnvelopeHelper;
import amp.messaging.IInboundProcessorCallback;
import cmf.bus.Envelope;
import cmf.bus.EnvelopeHeaderConstants;
import cmf.bus.IEnvelopeFilterPredicate;
import cmf.bus.IRegistration;
import cmf.eventing.patterns.streaming.StreamingEventItem;
import cmf.eventing.patterns.streaming.IStreamingReaderHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Specialized {@link cmf.bus.IRegistration} that handles the event by placing them in a
 * {@link java.util.Iterator} from which they can be extracted as they become available.
 * User: jholmberg
 * Date: 6/5/13
 */
public class StreamingReaderRegistration<TEVENT> implements IRegistration {
    protected static final Logger log = LoggerFactory.getLogger(StreamingReaderRegistration.class);

    protected IStreamingReaderHandler<TEVENT> eventHandler;
    protected IEnvelopeFilterPredicate filterPredicate;
    protected IInboundProcessorCallback processorCallback;
    protected  Map<String, String> registrationInfo;

    @Override
    public IEnvelopeFilterPredicate getFilterPredicate() {
        return filterPredicate;
    }

    @Override
    public Map<String, String> getRegistrationInfo() {
        return registrationInfo;
    }

    public StreamingReaderRegistration(IStreamingReaderHandler<TEVENT> handler, IInboundProcessorCallback processorCallback) {
        this.eventHandler = handler;
        this.processorCallback = processorCallback;

        registrationInfo = new HashMap<String, String>();
        registrationInfo.put(EnvelopeHeaderConstants.MESSAGE_TOPIC, eventHandler.getEventType().getCanonicalName());
    }

    @Override
    public Object handle(Envelope env) throws Exception {
        Object result = null;

        try {
            if (isEndOfStream(env)) {
                this.eventHandler.dispose();
            } else {
                TEVENT event = (TEVENT) this.processorCallback.ProcessInbound(env);
                if (null != event) {
                    StreamingEventItem<TEVENT> eventItem = new StreamingEventItem(event, env.getHeaders());
                    this.eventHandler.onEventRead(eventItem);
                }
            }
        } catch (Exception ex) {
          result = handleFailed(env, ex);
        }
        return result;
    }

    private boolean isEndOfStream(Envelope env) {
        EnvelopeHelper envelope = new EnvelopeHelper(env);
        String messageType = envelope.getMessageType();
        if (messageType.equals(EndOfStream.class.getCanonicalName())) {
            return true;
        }
        return false;
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
