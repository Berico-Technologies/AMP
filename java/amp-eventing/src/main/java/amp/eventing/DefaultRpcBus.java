package amp.eventing;


import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cmf.bus.Envelope;
import cmf.bus.EnvelopeHeaderConstants;
import cmf.bus.IEnvelopeBus;
import cmf.eventing.IEventHandler;
import cmf.eventing.patterns.rpc.IRpcEventBus;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import amp.messaging.EnvelopeHelper;
import amp.messaging.IContinuationCallback;
import amp.messaging.IInboundProcessorCallback;
import amp.messaging.IMessageProcessor;
import amp.messaging.MessageContext;
import amp.messaging.MessageContext.Directions;
import amp.messaging.MessageException;


public class DefaultRpcBus extends DefaultEventBus implements IRpcEventBus {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultRpcBus.class);

	IEnvelopeBus envelopeBus;
	
    public DefaultRpcBus(IEnvelopeBus envelopeBus, List<IMessageProcessor> inboundProcessors,
                    List<IMessageProcessor> outboundProcessors) {
        super(envelopeBus, inboundProcessors, outboundProcessors);
        this.envelopeBus = envelopeBus;
    }

    
    @Override
    public <TResponse> Collection<TResponse> gatherResponsesTo(Object request, Duration timeout) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Collection gatherResponsesTo(Object request, Duration timeout, String... expectedTopics) {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <TResponse> TResponse getResponseTo(Object request, Duration timeout, Class<TResponse> expectedType) {
        Object responseObject = getResponseTo(request, timeout, expectedType.getCanonicalName());

        return (TResponse) responseObject;
    }

    @Override
    public Object getResponseTo(Object request, final Duration timeout, final String expectedTopic) {
    	LOG.debug("Enter GetResponseTo");

        // guard clause
        if (null == request) {
            throw new IllegalArgumentException("Cannot get response to a null request");
        }

        // the container for the response we're going to get since you
        // can't assign the value of this within the continuation callback
        final MessageContext responseContext = new MessageContext(Directions.In);
        

        try {
        	// create the ID for the request
        	final UUID requestId = UUID.randomUUID();
        	
        	// Get an appropriately setup envelope
        	Envelope env = this.buildRequestEnvelope(requestId, timeout);

            // create an event context for processing
            final MessageContext context = new MessageContext(Directions.Out, env, request);
            
            // need a final-scoped handle to an IInboundProcessorCallback
            final IInboundProcessorCallback envelopeOpener = this._eventConsumer;
            
            // process the event
            this._eventProducer.getMessageProcessor().processMessage(
        		context, 
        		new IContinuationCallback() {

					@Override
					public void continueProcessing() throws MessageException {
					
						try {
							LOG.debug("successfully processed outgoing request");
						
							// create an RPC registration
				            RpcRegistration rpcRegistration = new RpcRegistration(requestId, expectedTopic, envelopeOpener);
	
				            // register with the envelope bus
				            envelopeBus.register(rpcRegistration);
	
				            // now that we're setup to receive the response, send the request
				            envelopeBus.send(context.getEnvelope());
	
				            // get the envelope from the registraton
				            responseContext.setMessage(rpcRegistration.getResponse(timeout));
	
				            // unregister from the bus
			            	envelopeBus.unregister(rpcRegistration);
						} catch (Exception ex) {
				        	LOG.error("Exception publishing an event", ex);
				            throw new MessageException("Exception publishing an event", ex);
						}
					} // end of final continuation
					
            }); // end of outbound processing
        } catch (Exception ex) {
        	LOG.error("Exception publishing an event", ex);
            throw new RuntimeException("Exception publishing an event", ex);
        }

        LOG.debug("Leave GetResponseTo");
        return responseContext.getMessage();
    }

    @Override
    public void respondTo(Map<String, String> headers, Object response) {
    	LOG.debug("Enter RespondTo");

        if (null == response) {
            throw new IllegalArgumentException("Cannot respond with a null event");
        }
        if (null == headers) {
            throw new IllegalArgumentException("Must provide non-null request headers");
        }

        Envelope envelope = new Envelope();
        envelope.setHeaders(headers);
        EnvelopeHelper originalHeadersHelper = new EnvelopeHelper(envelope);
        if (null == originalHeadersHelper.getMessageId()) {
            throw new IllegalArgumentException(
                            "Cannot respond to a request because the provided request headers do not contain a message ID");
        }

        try {
            Envelope env = new Envelope();
            new EnvelopeHelper(env).setCorrelationId(originalHeadersHelper.getMessageId());

            final MessageContext context = new MessageContext(Directions.Out, env, response);

            this._eventProducer.getMessageProcessor().processMessage(context, new IContinuationCallback() {
            
            	@Override
            	public void continueProcessing() throws MessageException {
            		try {
						envelopeBus.send(context.getEnvelope());
					} catch (Exception ex) {
			        	LOG.error("Exception publishing a response", ex);
			            throw new MessageException("Exception publishing a response", ex);
					}
            	}
            });

            
        } catch (Exception ex) {
        	LOG.error("Exception responding to an event", ex);
            throw new RuntimeException("Exception responding to an event", ex);
        }

        LOG.debug("Leave RespondTo");
    }


    protected Envelope buildRequestEnvelope(UUID requestId, Duration timeout) {
    	
    	// create a new envelope
        Envelope env = new Envelope();
        EnvelopeHelper envelopeHelper = new EnvelopeHelper(env);

        // set the requestId on the envelope
        envelopeHelper.setMessageId(requestId);

        // add pattern & timeout information to the headers
        envelopeHelper.setMessagePattern(EnvelopeHeaderConstants.MESSAGE_PATTERN_RPC);
        envelopeHelper.setRpcTimeout(timeout);
        
        return env;
    }
}
