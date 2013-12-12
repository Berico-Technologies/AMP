package amp.rabbit.connection;


import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RabbitConnectionShutdownListener implements ShutdownListener {

	private static final Logger logger = LoggerFactory.getLogger(RabbitConnectionShutdownListener.class);
	
	protected BaseConnectionFactory connectionFactory;
	
	//TODO: JM Complete refactor -- context is not what's listened to.
	protected ConnectionContext context;
	
	public RabbitConnectionShutdownListener(
			BaseConnectionFactory connectionFactory,
			ConnectionContext context) {
	
		this.connectionFactory = connectionFactory;
		this.context = context;
	}

	@Override
	public void shutdownCompleted(ShutdownSignalException ex) {
		
	//TODO: JM Restore this-- context isn't exactly what we listen to here...
	//	boolean removed = this.connectionFactory.removeConnection(context);
		boolean removed = false;
		if (removed == false){
			
			logger.warn("Could not find Channel for exchange '{}' in the pool.", context);
		}
	}

}
