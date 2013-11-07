package amp.policy.core;

import amp.utility.serialization.ISerializer;
import cmf.bus.Envelope;
import com.google.common.base.Optional;
import com.google.common.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adjudicator that can be extended to provide strongly typed Event's for enforcement.
 * @param <T> The Expected Event type.
 */
public abstract class EventAdjudicator<T> implements EnvelopeAdjudicator {

    /**
     * If there is no content type on the Envelope, we are going to assume it's JSON.
     */
    public static String DEFAULT_CONTENT_TYPE = "application/json";

    /**
     * This is Google Guava magic that will discover "T" at runtime so we can
     * deserialize to the most correct type.
     */
    protected TypeToken<T> eventTypeToken = new TypeToken<T>(getClass()){};

    /**
     * This is the factory that will provide the correct serializer based on Content-Type
     */
    protected SerializerFactory serializerFactory;

    // Trusted Logger.
    private static final Logger LOG = LoggerFactory.getLogger(EventAdjudicator.class);

    /**
     * Initialize the EventPolicy Adjudicator.
     * @param serializerFactory Factory that will produce the correct deserializer to use
     *                          when converting the payload.
     */
    public EventAdjudicator(SerializerFactory serializerFactory){

        this.serializerFactory = serializerFactory;
    }

    /**
     * All extending classes simply need to provide this implementation can immediately begin
     * enforcing policies.
     * @param event Deserialized payload.
     * @param envelope Original message.
     * @param enforcer The mechanism to enforce the policy.
     */
    public abstract void adjudicate(T event, Envelope envelope, PolicyEnforcer enforcer);

    /**
     * Adjudicate the message by delegating to derived classes.  This class will conveniently
     * deserialize the payload an provide it to derived classes.
     * @param envelope Envelope to adjudicate.
     * @param enforcer The mechanism to enforce the policy.
     */
    @Override
    public void adjudicate(Envelope envelope, PolicyEnforcer enforcer) {

        String contentType = Optional.of( envelope.getHeader("Content-Type") ).or(DEFAULT_CONTENT_TYPE);

        ISerializer serializer = serializerFactory.getByContentType(contentType);

        try {

            T event = (T) serializer.byteDeserialize(envelope.getPayload(), eventTypeToken.getRawType());

            adjudicate(event, envelope, enforcer);

        }
        catch (Exception e){

            enforcer.log(envelope, PolicyEnforcer.LogTypes.ERROR, "Could not convert body of message to an object.");
        }
    }
}
