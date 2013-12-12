package amp.rabbit.topology;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * RoutingInfo is the information needed by the Topology Client to creating connections
 * to brokers, and bind to exchanges and queues.
 * 
 * RoutingInfo includes both the information to produce, as well as, consume messages.  We
 * have made the decision to separate these concepts because in our model, we believe that
 * you may produce on one route and consume from another for security reasons.
 * 
 * We also force client to accept a collection of producing and consuming routes.  The intent
 * is that you may need to push/pull messages to/from multiple clusters based on topology.
 * 
 * @author Richard Clayton (Berico Technologies)
 */
public class RoutingInfo {

    protected ArrayList<ProducingRoute> producingRoutes = 
    		new ArrayList<ProducingRoute>();
    protected ArrayList<ConsumingRoute> consumingRoutes = 
    		new ArrayList<ConsumingRoute>();
    
    public RoutingInfo(){}
    public RoutingInfo(Collection<ProducingRoute> producingRoutes, 
    				   Collection<ConsumingRoute> consumingRoutes){
    	
    		this.producingRoutes.addAll(producingRoutes);
    		this.consumingRoutes.addAll(consumingRoutes);
    }
    
	public List<ProducingRoute> getProducingRoutes() {
		return producingRoutes;
	}
	
	public List<ConsumingRoute> getConsumingRoutes() {
		return consumingRoutes;
	}
	
	public List<BaseRoute> getAllRoutes() {
		ArrayList<BaseRoute> routes = new ArrayList<BaseRoute>();
		routes.addAll(producingRoutes);
		routes.addAll(consumingRoutes);
		return routes;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((consumingRoutes == null) ? 0 : consumingRoutes.hashCode());
		result = prime * result
				+ ((producingRoutes == null) ? 0 : producingRoutes.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RoutingInfo other = (RoutingInfo) obj;
		if (consumingRoutes == null) {
			if (other.consumingRoutes != null)
				return false;
		} else if (!consumingRoutes.equals(other.consumingRoutes))
			return false;
		if (producingRoutes == null) {
			if (other.producingRoutes != null)
				return false;
		} else if (!producingRoutes.equals(other.producingRoutes))
			return false;
		return true;
	}
}