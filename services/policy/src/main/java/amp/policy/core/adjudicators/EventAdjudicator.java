package amp.policy.core.adjudicators;

import amp.policy.core.EnvelopeAdjudicator;
import amp.policy.core.Enforcer;
import amp.policy.core.SerializerFactory;
import amp.utility.serialization.ISerializer;
import cmf.bus.Envelope;
import com.google.common.base.Strings;
import com.google.common.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
    final TypeToken<T> eventTypeToken = new TypeToken<T>(getClass()){};

    /**
     * This is the factory that will provide the correct serializer based on Content-Type
     */
    @Autowired
    protected SerializerFactory serializerFactory;

    // Trusted Logger.
    private static final Logger LOG = LoggerFactory.getLogger(EventAdjudicator.class);

    /**
     *  Default Constructor
     */
    public EventAdjudicator(){}

    /**
     * Initialize the EventPolicy Adjudicator.
     * @param serializerFactory Factory that will produce the correct deserializer to use
     *                          when converting the payload.
     */
    public EventAdjudicator(SerializerFactory serializerFactory){

        setSerializerFactory(serializerFactory);
    }

    /**
     * Set the SeralizerFactory needed to deserialize the event.
     * @param factory Factory that maps content-type to serializer.
     */
    public void setSerializerFactory(SerializerFactory factory){

        this.serializerFactory = factory;
    }

    /**
     * All extending classes simply need to provide this implementation can immediately begin
     * enforcing policies.
     * @param event Deserialized payload.
     * @param envelope Original message.
     * @param enforcer The mechanism to enforce the policy.
     */
    public abstract void adjudicate(T event, Envelope envelope, Enforcer enforcer);

    /**
     * Adjudicate the message by delegating to derived classes.  This class will conveniently
     * deserialize the payload an provide it to derived classes.
     * @param envelope Envelope to adjudicate.
     * @param enforcer The mechanism to enforce the policy.
     */
    @Override
    public void adjudicate(Envelope envelope, Enforcer enforcer) {

        String contentType = envelope.getHeader("Content-Type");

        if (Strings.isNullOrEmpty(contentType))
            contentType = DEFAULT_CONTENT_TYPE;

        ISerializer serializer = serializerFactory.getByContentType(contentType);

        try {

            T event = (T) serializer.byteDeserialize(envelope.getPayload(), eventTypeToken.getRawType());

            adjudicate(event, envelope, enforcer);

        }
        catch (Exception e){

            enforcer.log(envelope, Enforcer.LogTypes.ERROR, "Could not convert body of message to an object.");
        }
    }

    /**
     * Get the type of event this adjudicator evaluates.
     * @return Event Type.
     */
    public String getEventType(){
        return eventTypeToken.getRawType().toString();
    }
}
