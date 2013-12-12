package amp.examples.adaptor.integration;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cmf.bus.Envelope;
import cmf.bus.EnvelopeHeaderConstants;
import cmf.bus.IEnvelopeFilterPredicate;
import cmf.bus.IRegistration;

public class ResultsQueuingEnvelopeHandler implements IRegistration {
	
	private static final Logger logger = 
		LoggerFactory.getLogger(ResultsQueuingEnvelopeHandler.class);
	
	ResultsQueue resultsQueue;
	String topic;
	
	public ResultsQueuingEnvelopeHandler(ResultsQueue resultsQueue, String topic){
		
		this.resultsQueue = resultsQueue;
		this.topic = topic;
	}
	
	@Override
	public IEnvelopeFilterPredicate getFilterPredicate() {
		
		/**
		 * We only want messages from this process.
		 */
		return new IEnvelopeFilterPredicate() {
			@Override
			public boolean filter(Envelope envelope) {
				
				String sender = envelope.getHeader(
						EnvelopeHeaderConstants.MESSAGE_SENDER_IDENTITY);
				
				if (sender == null || !sender.equals(IntegrationUtils.PROCESS_IDENTITY)){
					
					return false;
				}
				
				return true;
			}
		};
	}

	@Override
	public Map<String, String> getRegistrationInfo() {
		
		Map<String, String> registrationInfo = new HashMap<String, String>();
		
		registrationInfo.put(EnvelopeHeaderConstants.MESSAGE_TOPIC, this.topic);
		
		return registrationInfo;
	}

	@Override
	public Object handle(Envelope envelope) throws Exception {
		
		logger.info("Message Received: {}", 
			envelope.getHeader(EnvelopeHeaderConstants.MESSAGE_TOPIC));
		
		this.resultsQueue.add(envelope);
		
		return true;
	}

	@Override
	public Object handleFailed(Envelope envelope, Exception e) throws Exception {
		
		logger.error("Error Handling Envelope: {}", e);
		
		return null;
	}

}
