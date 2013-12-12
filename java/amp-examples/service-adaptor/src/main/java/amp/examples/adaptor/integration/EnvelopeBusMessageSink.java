package amp.examples.adaptor.integration;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;

import cmf.bus.Envelope;
import cmf.bus.EnvelopeHeaderConstants;
import cmf.bus.IEnvelopeBus;

public class EnvelopeBusMessageSink implements MessageChannel {

	private static final Logger logger = LoggerFactory.getLogger(EnvelopeBusMessageSink.class);
	
	IEnvelopeBus envelopBus;
	String messageTopic;
	String messageType;
	
	public EnvelopeBusMessageSink(IEnvelopeBus envelopBus, String messageTopic, String messageType){
	
		this.envelopBus = envelopBus;
		this.messageTopic = messageTopic;
		this.messageType = messageType;
	}
	
	@Override
	public boolean send(Message<?> message) {
		
		// No support for timeouts.
		return this.send(message, -1l);
	}

	@Override
	public boolean send(Message<?> message, long timeout) {
		
		logger.info("Adapting and sending message with ID: {}", message.getHeaders().getId());
		
		logger.debug("Payload: {}", message.getPayload());
		
		HashMap<String, String> amplifyingHeaders = new HashMap<String, String>();
		
		amplifyingHeaders.put(
			EnvelopeHeaderConstants.MESSAGE_SENDER_IDENTITY, IntegrationUtils.PROCESS_IDENTITY);
		amplifyingHeaders.put(EnvelopeHeaderConstants.MESSAGE_TOPIC, this.messageTopic);
		amplifyingHeaders.put(EnvelopeHeaderConstants.MESSAGE_TYPE, this.messageType);
		
		Envelope envelope = IntegrationUtils.messageToEnvelope(message, amplifyingHeaders);
		
		try {
			
			this.envelopBus.send(envelope);
			
		} catch (Exception e) {
			
			logger.error("Error sending envelope: {}", e);
			
			return false;
		}
		
		return true;
	}	
}