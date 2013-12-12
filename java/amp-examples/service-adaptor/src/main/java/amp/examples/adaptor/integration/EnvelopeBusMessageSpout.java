package amp.examples.adaptor.integration;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;

import cmf.bus.Envelope;
import cmf.bus.EnvelopeHeaderConstants;
import cmf.bus.IEnvelopeBus;
import cmf.bus.IEnvelopeFilterPredicate;
import cmf.bus.IRegistration;

public class EnvelopeBusMessageSpout  {
	
	private static final Logger logger = LoggerFactory.getLogger(EnvelopeBusMessageSpout.class);
	
	IEnvelopeBus envelopeBus; 
	MessageChannel messageChannel;
	String topic;
	
	public EnvelopeBusMessageSpout(
		IEnvelopeBus envelopeBus, String topic, MessageChannel messageChannel){
		
		this.envelopeBus = envelopeBus;
		this.topic = topic;
		this.messageChannel = messageChannel;
		
		logger.info("Channel is null?: {}", messageChannel);
		
	}
	
	public void initialize() throws Exception {
		
		this.envelopeBus.register(new IRegistration(){

			@Override
			public IEnvelopeFilterPredicate getFilterPredicate() {
				
				return new IEnvelopeFilterPredicate(){

					@Override
					public boolean filter(Envelope envelope) {
						
						return true;
					}
				};
			}

			@Override
			public Map<String, String> getRegistrationInfo() {
				
				Map<String, String> registrationInfo = new HashMap<String, String>();
				
				registrationInfo.put(EnvelopeHeaderConstants.MESSAGE_TOPIC, topic);
				
				return registrationInfo;
			}

			@Override
			public Object handle(Envelope envelope) throws Exception {
				
				Message<String> message = IntegrationUtils.envelopeToMessage(envelope);
				
				return messageChannel.send(message);
			}

			@Override
			public Object handleFailed(Envelope envelope, Exception e)
					throws Exception {
				
				logger.error("Error adapting message: {}", e);
				
				return null;
			}
		});
	}
}
