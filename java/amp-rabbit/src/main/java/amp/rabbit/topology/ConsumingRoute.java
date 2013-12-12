package amp.rabbit.topology;

import java.util.Arrays;
import java.util.Collection;

/**
 * Represents the information needed to consume messages by the transport provider.
 * 
 * @author Richard Clayton (Berico Technologies)
 */
public class ConsumingRoute extends BaseRoute {

	Queue queue; 

	public ConsumingRoute(){}
	
	public ConsumingRoute(Collection<Broker> brokers, Exchange exchange,
			Queue queue, Collection<String> routingKeys) {
		super(brokers, exchange, routingKeys);		
		this.queue = queue;
	}
	
	public Queue getQueue() {
		return queue;
	}

	public void setQueue(Queue queue) {
		this.queue = queue;
	}

	@Override
	public String toString() {
		return "ConsumingRoute [queue=" + queue + ", brokers=" + brokers
				+ ", exchange=" + exchange + ", routingKey=" + routingKeys + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((queue == null) ? 0 : queue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		//Not needed.  Super tests this.
		//if (getClass() != obj.getClass())
		//	return false;
		ConsumingRoute other = (ConsumingRoute) obj;
		if (queue == null) {
			if (other.queue != null)
				return false;
		} else if (!queue.equals(other.queue))
			return false;
		return true;
	}

	public static ConsumingRouteBuilder builder(){
		
		return new ConsumingRouteBuilder();
	}
	
	public static class ConsumingRouteBuilder {
		
		ConsumingRoute consumingRoute = new ConsumingRoute();
		
		public ConsumingRouteBuilder brokers(Broker... brokers){
			
			consumingRoute.setBrokers(Arrays.asList(brokers));
			
			return this;
		}
		
		public ConsumingRouteBuilder brokers(Collection<Broker> brokers){
			
			consumingRoute.setBrokers(brokers);
			
			return this;
		}
		
		public ConsumingRouteBuilder exchange(Exchange exchange){
			
			consumingRoute.setExchange(exchange);
			
			return this;
		}
		
		public ConsumingRouteBuilder queue(Queue queue){
			
			consumingRoute.setQueue(queue);
			
			return this;
		}
		
		public ConsumingRouteBuilder routingkeys(String... routingkeys){
			
			consumingRoute.setRoutingKeys(Arrays.asList(routingkeys));
			
			return this;
		}
		
		public ConsumingRouteBuilder routingkeys(Collection<String> routingkeys){
			
			consumingRoute.setRoutingKeys(routingkeys);
			
			return this;
		}
		
		public ConsumingRoute build(){
			
			return this.consumingRoute;
		}
	}
}

