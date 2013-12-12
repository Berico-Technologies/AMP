package amp.rabbit.topology;

import java.util.Arrays;
import java.util.Collection;

/**
 * Represents the information required by the transport to produce
 * a message.
 * 
 * @author Richard Clayton (Berico Technologies)
 */
public class ProducingRoute extends BaseRoute {
	
	public ProducingRoute() { super(); }

	public ProducingRoute(Collection<Broker> brokers, Exchange exchange, Collection<String> routingKeys) {
		
		super(brokers, exchange, routingKeys);
	}
	
	@Override
	public String toString() {
		return "ProducingRoute [brokers=" + brokers + ", exchange=" + exchange
				+ ", routingKey=" + routingKeys + "]";
	}
	
	public static ProducingRouteBuilder builder(){
		
		return new ProducingRouteBuilder();
	}
	
	public static class ProducingRouteBuilder {
		
		ProducingRoute producingRoute = new ProducingRoute();
		
		public ProducingRouteBuilder exchange(Exchange exchange){
			
			this.producingRoute.setExchange(exchange);
			
			return this;
		}
		
		public ProducingRouteBuilder brokers(Collection<Broker> brokers){
			
			this.producingRoute.setBrokers(brokers);
			
			return this;
		}
		
		public ProducingRouteBuilder brokers(Broker... brokers){
					
			this.producingRoute.setBrokers(Arrays.asList(brokers));
			
			return this;
		}
		
		public ProducingRouteBuilder routingKeys(String... routingKeys){
			
			this.producingRoute.setRoutingKeys(Arrays.asList(routingKeys));
			
			return this;
		}
		
		public ProducingRouteBuilder routingKeys(Collection<String> routingKeys){
			
			this.producingRoute.setRoutingKeys(routingKeys);
			
			return this;
		}
		
		public ProducingRoute build(){
			
			return this.producingRoute;
		}
	}
}

