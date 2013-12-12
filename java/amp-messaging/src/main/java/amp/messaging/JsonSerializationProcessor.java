package amp.messaging;


import cmf.bus.Envelope;
import amp.messaging.EnvelopeHeaderConstants;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created with IntelliJ IDEA.
 * User: jar349
 * Date: 5/14/13
 */
public class JsonSerializationProcessor implements IMessageProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(JsonSerializationProcessor.class);
    private static final String ENCODING = "UTF-8";


    private class DotNetTypeExclusionStrategy implements ExclusionStrategy {

        @Override
        public boolean shouldSkipClass(Class<?> arg0) {
            return false; // don't skip any classes
        }

        @Override
        public boolean shouldSkipField(FieldAttributes arg0) {
            return "$type".equals(arg0.getName()); // ignore fields named $type which may be added by the dotnet serializer
        }

    }


    private class GsonIgnoreExclusionStrategy implements ExclusionStrategy {
        private final Class<?> typeToSkip;

        private GsonIgnoreExclusionStrategy(Class<?> typeToSkip) {
            this.typeToSkip = typeToSkip;
        }

        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            return f.getAnnotation(GsonIgnore.class) != null;
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return (clazz == typeToSkip);
        }
    }


    private Gson gson = new GsonBuilder()
            .enableComplexMapKeySerialization()
            .setExclusionStrategies(
                    new DotNetTypeExclusionStrategy(),
                    new GsonIgnoreExclusionStrategy(GsonIgnore.class)
            ).create();


    @Override
    public void processMessage(MessageContext context, IContinuationCallback next) throws MessageException {

        if (MessageContext.Directions.In == context.getDirection()) {
    		this.processInbound(context, next);
    	}
    	else if (MessageContext.Directions.Out == context.getDirection()) {
    		this.processOutbound(context, next);
    	}
    }


    public void processInbound(MessageContext context, IContinuationCallback next) throws MessageException {

        Envelope env = context.getEnvelope();

        String eventType = env.getHeader(EnvelopeHeaderConstants.MESSAGE_TYPE);

        try {
            Class<?> type = Class.forName(eventType);

            String jsonString = new String(env.getPayload(), ENCODING);
            LOG.debug("Will deserialize into a message: " + jsonString);

            context.setMessage(this.gson.fromJson(jsonString, type));

            next.continueProcessing();
        }
        catch (Exception ex) {
            String message = "Failed to deserialize an incoming message.";
            LOG.error(message, ex);
            throw new MessageException(message, ex);
        }
    }

    public void processOutbound(MessageContext context, IContinuationCallback next) throws MessageException {

        Envelope env = context.getEnvelope();

        try {
            String jsonString = gson.toJson(context.getMessage());
            LOG.debug("Serialized message: " + jsonString);

            env.setPayload(jsonString.getBytes(ENCODING));

            next.continueProcessing();
        }
        catch (Exception ex) {
            String message = "Failed to serialize an outgoing message.";
            LOG.error(message, ex);
            throw new MessageException(message, ex);
        }
    }
}
