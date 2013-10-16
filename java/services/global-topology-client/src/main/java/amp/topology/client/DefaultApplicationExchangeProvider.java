package amp.topology.client;

import java.util.Collection;
import java.util.Map;

import amp.rabbit.topology.Broker;
import amp.rabbit.topology.RoutingInfo;
import amp.rabbit.topology.SimpleTopologyService;

/**
 * Provides a default route to use when no routing information returns back from the Global Topology Service.
 * In this case, the default application provider uses a default exchange and queue prototype, inherited from
 * the SimpleTopologyService implementation.
 * 
 * @author Richard Clayton (Berico Technologies)
 */
public class DefaultApplicationExchangeProvider extends SimpleTopologyService implements FallbackRoutingInfoProvider {
	
	public DefaultApplicationExchangeProvider(){}
	
	public DefaultApplicationExchangeProvider(String clientProfile, String hostname) {
		
		super(clientProfile, new Broker("default", hostname, 5672, false));
	}
	
	public DefaultApplicationExchangeProvider(String clientProfile, String hostname, int port) {
		
		super(clientProfile, new Broker("default", hostname, port, false));
	}
	
	public DefaultApplicationExchangeProvider(String clientProfile, String hostname, int port, boolean useSsl) {
		
		super(clientProfile, new Broker("default", hostname, port, useSsl));
	}
	
	public DefaultApplicationExchangeProvider(String clientProfile, Collection<Broker> brokers) {
		
		super(clientProfile, brokers);
	}

	public DefaultApplicationExchangeProvider(String clientProfile, Broker... brokers) {
		
		super(clientProfile, brokers);
	}

	/**
	 * Convenience method to set the exchange name on the prototype.
	 * @param exchangeName Name of the exchange.
	 */
	public void setExchangeName(String exchangeName){
		
		this.getExchangePrototype().setName(exchangeName);
	}
	
	/**
	 * Convenience method to set the exchange type on the prototype.
	 * @param exchangeType Type of the exchange.
	 */
	public void setExchangeType(String exchangeType){
		
		this.getExchangePrototype().setExchangeType(exchangeType);
	}
	
	/**
	 * Convenience method to set the virtual host on the prototype.
	 * @param virtualHost Name of the virtual host.
	 */
	public void setVirtualHost(String virtualHost){
		
		this.getExchangePrototype().setVirtualHost(virtualHost);
	}
	
	/**
	 * Convenience method to set the queue name on the prototype.
	 * @param queueName Name of the queue.
	 */
	public void setQueueName(String queueName){
		
		this.getQueuePrototype().setName(queueName);
	}
	
	/**
	 * Get the fallback route, in this case the default
	 * exchange and a routing key equal to the topic.
	 * 
	 * @param topic Topic of the message.
	 */
	@Override
	public RoutingInfo getFallbackRoute(Map<String, String> routingHints) {
		
		return this.getRoutingInfo(routingHints);
	}
	
}
