package amp.topology.core;

import java.util.Collection;

public interface ITopologyRepository {
	
	ExtendedExchange getExchange(String id);
	
	ExtendedRouteInfo getRoute(String id);
	
	Collection<ExtendedExchange> getExchanges();
	
	Collection<ExtendedExchange> getExchangesByBroker(String host);
	
	Collection<ExtendedExchange> getExchangesByBroker(String host, int port);
	
	Collection<ExtendedExchange> getExchangesByBroker(String host, int port, String vhost);
	
	Collection<Broker> getBrokers();
	
	Collection<ExtendedRouteInfo> getRoutes();
	
	Collection<ExtendedRouteInfo> getRoutesByTopic(String topic);
	
	Collection<ExtendedRouteInfo> getRoutesByClient(String client);
	
	Collection<String> getTopics();
	
	Collection<String> getClients();
	
	void createExchange(ExtendedExchange exchange);
	
	void createRoute(ExtendedRouteInfo routeInfo);
	
	void updateExchange(ExtendedExchange exchange);
	
	void updateRoute(ExtendedRouteInfo routeInfo);
	
	boolean removeExchange(String id);
	
	boolean removeRoute(String id);
	
	void purge();
	
	Collection<ExtendedRouteInfo> find(String topic, String client);
	
	void addEventListener(ITopologyRepositoryEventListener listener);
	
	void removeEventListener(ITopologyRepositoryEventListener listener);
}
