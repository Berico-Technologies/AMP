package amp.topology.client;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import amp.rabbit.topology.Exchange;
import amp.rabbit.topology.RouteInfo;
import amp.rabbit.topology.RoutingInfo;

/**
 * Provides a route on the "amp.fallback" exchange.  The implementation will not
 * provide a queue name, assuming the transport will create a unique queue instead
 * (we don't want round-robin on a single queue).  The exchange provided is "direct"
 * by default, meaning delivery of messages will require an exact match (that is,
 * routing key = topic). You will not be able to consume multiple event types on the
 * same queue unless you change the default AMP implementation.
 * 
 * @author Richard Clayton (Berico Technologies)
 */
public class DefaultApplicationExchangeProvider implements FallbackRoutingInfoProvider {

	protected static long UNIQUE_PROCESS_ID = 0;
	protected String clientName = UUID.randomUUID().toString();
	protected String exchangeName = "amp.fallback";
	protected String hostname = "localhost";
	protected String vhost = "/";
	protected int port = 5672;
	protected String exchangeType = "direct";
	protected String queueName = null;
	protected boolean isDurable = false;
	protected boolean isAutoDelete = true;
	
	@SuppressWarnings("rawtypes")
	protected Map arguments = null;
	
	/**
	 * Not going to overload this beast, so please use the setters to configure
	 * any non-default options.
	 */
	public DefaultApplicationExchangeProvider(){}
	
	/**
	 * Get the fallback route, in this case the default
	 * exchange and a routing key equal to the topic.
	 * 
	 * @param topic Topic of the message.
	 */
	@Override
	public RoutingInfo getFallbackRoute(String topic) {
		
		String queue = this.queueName;
		
		if (queueName == null){
			
			queue = String.format("%s#%03d#%s", this.clientName, ++UNIQUE_PROCESS_ID, topic);
		}
		
		Exchange defaultExchange = new Exchange(
				this.exchangeName, this.hostname, this.vhost, 
				this.port, topic, queue, this.exchangeType, 
				this.isDurable, this.isAutoDelete, this.arguments);
		
		RouteInfo theOnlyRoute = new RouteInfo(defaultExchange, defaultExchange);
	
		ArrayList<RouteInfo> routes = new ArrayList<RouteInfo>();
		routes.add(theOnlyRoute);
		
		return new RoutingInfo(routes);
	}
	
	public String getExchangeName() {
		return exchangeName;
	}

	public void setExchangeName(String exchangeName) {
		this.exchangeName = exchangeName;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getVhost() {
		return vhost;
	}

	public void setVhost(String vhost) {
		this.vhost = vhost;
	}

	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}
	
	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getExchangeType() {
		return exchangeType;
	}

	public void setExchangeType(String exchangeType) {
		this.exchangeType = exchangeType;
	}
	
	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public boolean isDurable() {
		return isDurable;
	}

	public void setDurable(boolean isDurable) {
		this.isDurable = isDurable;
	}

	public boolean isAutoDelete() {
		return isAutoDelete;
	}

	public void setAutoDelete(boolean isAutoDelete) {
		this.isAutoDelete = isAutoDelete;
	}
	
	@SuppressWarnings("rawtypes")
	public Map getArguments() {
		return arguments;
	}
	
	@SuppressWarnings("rawtypes")
	public void setArguments(Map arguments) {
		this.arguments = arguments;
	}

}
