package amp.rabbit;


import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import amp.rabbit.topology.Exchange;


public class RabbitConnectionShutdownListener implements ShutdownListener {

	private static final Logger logger = LoggerFactory.getLogger(RabbitConnectionShutdownListener.class);
	
	protected BaseChannelFactory channelFactory;
	
	protected Exchange exchange;
	
	public RabbitConnectionShutdownListener(
			BaseChannelFactory channelFactory,
			Exchange exchange) {
	
		this.channelFactory = channelFactory;
		this.exchange = exchange;
	}

	@Override
	public void shutdownCompleted(ShutdownSignalException ex) {
		
		boolean removed = this.channelFactory.removeConnection(exchange);
		
		if (removed == false){
			
			logger.warn("Could not find Channel for exchange '{}' in the pool.", exchange.getName());
		}
	}

}
