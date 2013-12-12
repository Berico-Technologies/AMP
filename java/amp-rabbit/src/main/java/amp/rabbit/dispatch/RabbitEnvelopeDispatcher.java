package amp.rabbit.dispatch;

import java.io.IOException;

import cmf.bus.Envelope;
import cmf.bus.IRegistration;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import amp.bus.IEnvelopeDispatcher;


public class RabbitEnvelopeDispatcher implements IEnvelopeDispatcher {
	
    protected Channel channel;
    protected long deliveryTag;
    protected Envelope env;
    protected Logger log;
    protected IRegistration registration;

    public RabbitEnvelopeDispatcher(IRegistration registration, Envelope env, Channel channel, long deliveryTag) {

        this.registration = registration;
        this.env = env;
        this.channel = channel;
        this.deliveryTag = deliveryTag;

        log = LoggerFactory.getLogger(this.getClass());
    }

    @Override
    public void dispatch() {
        log.debug("Enter Dispatch()");
        this.dispatch(env);
        log.debug("Leave Dispatch()");
    }

    @Override
    public void dispatch(Envelope envelope) {
        
		log.debug("Enter Dispatch(env)");
        
        Object maybeNull = null;

        try {
            // this may be null, or it may be any other kind of object
            maybeNull = registration.handle(env);
            
            log.debug("Dispatched envelope to registration");
            
        	respondToMessage(maybeNull);
            
        } catch (Exception ex) {
        		
            log.error("Caught an unhandled exception dispatching an envelope", ex);
            
            dispatchFailed(this.env, ex);
        }
        
        log.debug("Leave Dispatch(env)");
    }

    @Override
    public void dispatchFailed(Envelope envelope, Exception e) {
    
		log.debug("Dispatching envelope to fail handler.");
	
		Object maybeNull = null;
		
		try {
			
			maybeNull = this.registration.handleFailed(envelope, e);
			
			log.debug("Dispatched envelope to registration fail handler.");

		} catch (Exception ex){
			
			log.error("Registration fail handler threw an exception.", ex);
			
			maybeNull = DeliveryOutcomes.Exception;
			
		} finally {
			
			respondToMessage(maybeNull);
		}	
    }

    protected void respondToMessage(Object maybeNull) {
        log.debug("Enter RespondToMessage");

        // we accept an envelope instead of dispatching the envelope in our
        // state because whoever is consuming us may have mutated it
        DeliveryOutcomes result = DeliveryOutcomes.Null;

        // by convention, if handlers return nothing, assume acknowledgement
        // if the object is not null and a DeliveryOutcome, cast it
        // else, assume acknowledgement
        if (null == maybeNull) {
        	
            result = DeliveryOutcomes.Acknowledge;
            
        } else if (maybeNull instanceof DeliveryOutcomes) {
        	
            result = (DeliveryOutcomes) maybeNull;
            
        } else {
        	
            result = DeliveryOutcomes.Acknowledge;
        }

        log.info("DeliveryOutcome of handled event is: {}", result);
        
        try {
        
	        switch (result) {
	            case Acknowledge:
	                channel.basicAck(deliveryTag, false);
	                break;
	            case Null:
	                channel.basicAck(deliveryTag, false);
	                break;
	            case Reject:
	                channel.basicReject(deliveryTag, false);
	                break;
	            case Exception:
	                channel.basicNack(deliveryTag, false, false);
	                break;
	        }
	        
        } catch (IOException ioex) {
        	
    		log.error("There was a problem sending delivery outcome back to broker.", ioex);
        }

        log.debug("Leave RespondToMessage");
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void setDeliveryTag(long deliveryTag) {
        this.deliveryTag = deliveryTag;
    }

    public void setEnvelope(Envelope env) {
        this.env = env;
    }

    public void setRegistration(IRegistration registration) {
        this.registration = registration;
    }
    
    public Channel getChannel() {
        return channel;
    }

    public long getDeliveryTag() {
        return deliveryTag;
    }

    @Override
    public Envelope getEnvelope() {
        return env;
    }

    public IRegistration getRegistration() {
        return registration;
    }
}
