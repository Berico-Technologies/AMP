package amp.examples.adaptor.integration;

import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.support.MessageBuilder;

import amp.messaging.EnvelopeHelper;
import cmf.bus.Envelope;

public class IntegrationUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(IntegrationUtils.class);
	
	public static String PROCESS_IDENTITY = UUID.randomUUID().toString();
	
	/**
	 * Convert a Sprint Integration Message to an CMF Envelope.
	 * @param message Message to Convert
	 * @return CMF Envelope
	 */
	public static Envelope messageToEnvelope(Message<?> message, Map<String, String> amplifyingHeaders){
		
		logger.debug("Adapting message with ID, {}, to an Envelope.", message.getHeaders().getId());
		
		MessageHeaders mh = message.getHeaders();
		
		EnvelopeHelper eh = new EnvelopeHelper();
		
		eh.setMessageId(mh.getId());
		
		if (mh.getCorrelationId() != null){
		
			try {
				
				UUID correlationId = UUID.fromString(mh.getCorrelationId().toString());
			
				eh.setCorrelationId(correlationId);
			
			} catch (IllegalArgumentException iae){
				
				logger.warn("Correlation ID could not be converted: {}", iae);
			}
		}
		
		eh.setCreationTime(new DateTime(mh.getTimestamp()));
		
		for (Entry<String, Object> header : mh.entrySet()){
			
			eh.setHeader(header.getKey(), header.getValue().toString());
		}
		
		eh.setPayload(message.getPayload().toString().getBytes());
		
		if (amplifyingHeaders != null && amplifyingHeaders.size() > 0){
			
			for(Entry<String, String> header : amplifyingHeaders.entrySet()){
				
				eh.setHeader(header.getKey(), header.getValue());
			}
		}
		
		return eh.getEnvelope();
	}
	
	/**
	 * Convert a CMF Envelope to a Spring Integration Message.
	 * @param envelope Envelope to convert.
	 * @return Spring Integration Message.
	 */
	public static Message<String> envelopeToMessage(Envelope envelope){
		
		EnvelopeHelper eh = new EnvelopeHelper(envelope);
		
		MessageBuilder<String> mb = MessageBuilder.withPayload(new String(envelope.getPayload()));
		
		mb.setCorrelationId(eh.getMessageId());
		
		for (Entry<String, String> header : envelope.getHeaders().entrySet()){
			
			mb.setHeader(header.getKey(), header.getValue());
		}
		
		return mb.build();
	}
}