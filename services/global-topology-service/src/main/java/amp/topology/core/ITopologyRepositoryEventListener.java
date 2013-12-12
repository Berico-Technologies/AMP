package amp.topology.core;

import java.util.Collection;

public interface ITopologyRepositoryEventListener {

	void routeCreated(ExtendedRouteInfo routeInfo);
	
	void routeRemoved(ExtendedRouteInfo routeInfo);
	
	void routeUpdated(ExtendedRouteInfo oldRouteInfo, ExtendedRouteInfo newRouteInfo);
	
	void exchangeCreated(ExtendedExchange exchange);
	
	void exchangeRemoved(ExtendedExchange exchange);
	
	void exchangeUpdated(ExtendedExchange oldExchange, ExtendedExchange newExchange);
	
	void routingInfoRetrieved(String topic, String client, Collection<ExtendedRouteInfo> routingInfo);
}
