package amp.rabbit;


import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConnectionRemediator extends Thread {

    public static long RETRY_INTERVAL = 30 * 1000;
	private static final Logger logger = LoggerFactory.getLogger(ConnectionRemediator.class);
	
	private RabbitListener listener;
	private IRabbitChannelFactory channelFactory;
    private boolean keepTrying = true;

	
	public ConnectionRemediator(
			RabbitListener listener,
			IRabbitChannelFactory channelFactory) {
		
		this.listener = listener;
		this.channelFactory = channelFactory;
	}
	
	public ConnectionRemediator(
			RabbitListener listener,
			IRabbitChannelFactory channelFactory,
			long retryInterval) {
		
		this.listener = listener;
		this.channelFactory = channelFactory;
		RETRY_INTERVAL = retryInterval;
	}


	@Override
	public void run() {
		
		while (keepTrying) {
			
			logger.debug("Attempting to reconnect to broker.");
			
			Channel channel = null;
			
			try {
				
				channel = this.channelFactory.getChannelFor(listener.getExchange());
				
				listener.start(channel);
				
				logger.info("Connection re-established with broker.");
				
				// Yeah, we've reconnected!
				stopTrying();
				
			} catch (Exception e) {
				
				logger.warn("Could not reconnect to broker.", e);
				
				// Make sure channel is null if we failed somewhere.
				channel = null;
			}
			
			if (channel == null) { nap(); }
		}
	}
	
	/**
	 * Set the time in ms between retries.
	 * @param interval time in ms
	 */
	public void setRetryInterval(long interval){
		
		RETRY_INTERVAL = interval;
	}
	
	/**
	 * Stop trying to reconnect.
	 */
	public void stopTrying(){
		
		this.keepTrying = false;
	}


	/**
	 * Take a break.  This is basically "Thread.sleep",
	 * wrapped in a try-catch.
	 */
	protected void nap(){
		
		try {
			
			Thread.sleep(RETRY_INTERVAL);
			
		} catch (InterruptedException e) {
			
			logger.error("Thread was interrupted.", e);
			
			stopTrying();
		}
	}
}
