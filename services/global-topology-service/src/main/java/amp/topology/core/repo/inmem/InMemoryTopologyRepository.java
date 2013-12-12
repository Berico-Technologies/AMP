package amp.topology.core.repo.inmem;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import amp.topology.core.BaseTopologyRepository;
import amp.topology.core.Broker;
import amp.topology.core.ExtendedExchange;
import amp.topology.core.ExtendedRouteInfo;
import amp.topology.core.ITopologyRepository;
import amp.topology.core.ITopologyRepositoryEventListener;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

/**
 * The simplest Repository one could use (keeping everything in memory).
 * This implementation is only recommended for use in development.  You can
 * make the state persistent by pairing the Repository with the TopologySnapshot
 * facility.
 * 
 * @author Richard Clayton (Berico Technologies)
 */
public class InMemoryTopologyRepository extends BaseTopologyRepository implements ITopologyRepository {

	ConcurrentHashMap<String, ExtendedExchange> exchanges = 
			new ConcurrentHashMap<String, ExtendedExchange>();
	
	ConcurrentHashMap<String, ExtendedRouteInfo> routes = 
			new ConcurrentHashMap<String, ExtendedRouteInfo>();
	
	public InMemoryTopologyRepository(){}
	
	public InMemoryTopologyRepository(
			Collection<ITopologyRepositoryEventListener> listeners) {
		
		super(listeners);
	}

	public InMemoryTopologyRepository(
		Collection<ExtendedExchange> exchanges, Collection<ExtendedRouteInfo> routes){
		
		super();
		
		setExchanges(exchanges);
		setRoutes(routes);
	}
	
	public InMemoryTopologyRepository(
			Collection<ITopologyRepositoryEventListener> listeners,
			ConcurrentHashMap<String, ExtendedExchange> exchanges,
			ConcurrentHashMap<String, ExtendedRouteInfo> routes) {
		
		super(listeners);
		
		this.exchanges = exchanges;
		this.routes = routes;
	}
	
	public void setExchanges(Collection<ExtendedExchange> exchangesToSet){
		
		for (ExtendedExchange exchange : exchangesToSet){
			
			exchanges.put(exchange.getId(), exchange);
		}
	}
	
	public void setRoutes(Collection<ExtendedRouteInfo> routesToSet){
		
		for (ExtendedRouteInfo route : routesToSet){
			
			routes.put(route.getId(), route);
		}
	}
	
	@Override
	public ExtendedExchange getExchange(String id) {
		
		return exchanges.get(id);
	}

	@Override
	public ExtendedRouteInfo getRoute(String id) {
		
		return routes.get(id);
	}

	@Override
	public Collection<ExtendedExchange> getExchanges() {
		
		return exchanges.values();
	}

	@Override
	public Collection<ExtendedExchange> getExchangesByBroker(final String host) {
		
		return this.filterExchanges(new Predicate<ExtendedExchange>(){
			@Override
			public boolean apply(@Nullable ExtendedExchange exchange) {
				
				return exchange.getHostName().equalsIgnoreCase(host);
			}
		});
	}

	@Override
	public Collection<ExtendedExchange> getExchangesByBroker(final String host, final int port) {
		
		return this.filterExchanges(new Predicate<ExtendedExchange>(){
			@Override
			public boolean apply(@Nullable ExtendedExchange exchange) {
				
				return exchange.getHostName().equalsIgnoreCase(host) && exchange.getPort() == port;
			}
		});
	}

	@Override
	public Collection<ExtendedExchange> getExchangesByBroker(
			final String host, final int port, final String vhost) {
		
		return this.filterExchanges(new Predicate<ExtendedExchange>(){
			@Override
			public boolean apply(@Nullable ExtendedExchange exchange) {
				
				return exchange.getHostName().equalsIgnoreCase(host) 
						&& exchange.getPort() == port
						&& exchange.getVirtualHost().equalsIgnoreCase(vhost);
			}
		});
	}
	
	@Override
	public Collection<ExtendedRouteInfo> getRoutes() {
		
		return routes.values();
	}

	@Override
	public Collection<ExtendedRouteInfo> getRoutesByTopic(final String topic) {
		
		return filterRoutes(new Predicate<ExtendedRouteInfo>(){

			@Override
			public boolean apply(@Nullable ExtendedRouteInfo route) {
				
				return route.getTopics().contains(topic);
			}
		});
	}

	@Override
	public Collection<ExtendedRouteInfo> getRoutesByClient(final String client) {
		
		return filterRoutes(new Predicate<ExtendedRouteInfo>(){

			@Override
			public boolean apply(@Nullable ExtendedRouteInfo route) {
				
				return route.getClients().contains(client);
			}
		});
	}
	
	@Override
	public void createExchange(ExtendedExchange exchange) {
		
		exchanges.put(exchange.getId(), exchange);
		
		fireExchangeCreated(exchange);
	}

	@Override
	public void createRoute(ExtendedRouteInfo routeInfo) {
		
		routes.put(routeInfo.getId(), routeInfo);
		
		fireRouteCreated(routeInfo);
	}

	@Override
	public void updateExchange(ExtendedExchange exchange) {
		
		ExtendedExchange oldExchange = exchanges.replace(exchange.getId(), exchange);
		
		if (oldExchange != null){
			
			fireExchangeUpdated(oldExchange, exchange);
		}
	}

	@Override
	public void updateRoute(ExtendedRouteInfo routeInfo) {
		
		ExtendedRouteInfo oldRouteInfo = routes.replace(routeInfo.getId(), routeInfo);
		
		if (oldRouteInfo != null){
			
			fireRouteUpdated(oldRouteInfo, routeInfo);
		}
	}

	@Override
	public boolean removeExchange(String id) {
		
		ExtendedExchange exchange = exchanges.remove(id);
		
		if (exchange != null){
			
			fireExchangeRemoved(exchange);
		}
		
		return exchange != null;
	}

	@Override
	public boolean removeRoute(String id) {
		
		ExtendedRouteInfo routeInfo = routes.remove(id);
		
		if (routeInfo != null){
			
			fireRouteRemoved(routeInfo);
		}
		
		return routeInfo != null;
	}

	/**
	 * Find all routes matching the supplied topic and client.
	 * @param topic Name of the topic or route.
	 * @param client Name of the client.
	 * @return Routing info for that topic-client pair.
	 */
	@Override
	public Collection<ExtendedRouteInfo> find(final String topic, final String client) {
		
		Collection<ExtendedRouteInfo> matchingRoutes = filterRoutes(new Predicate<ExtendedRouteInfo>(){

			@Override
			public boolean apply(@Nullable ExtendedRouteInfo route) {
				
				return route.getClients().contains(client) && route.getTopics().contains(topic);
			}
			
		});
		
		fireRoutingInfoRetrieved(topic, client, matchingRoutes);
		
		return matchingRoutes;
	}

	/**
	 * Clear all topology information from the repository.
	 */
	@Override
	public void purge() {
		
		this.exchanges.clear();
		this.routes.clear();
	}
	
	/**
	 * A much easier way to perform half the filtering/query functions needed on the
	 * ExtendedExchange in-memory collection.
	 * @param predicate Predicate with the filter logic for ExtendedExchange.
	 * @return A collection of matching ExtendedExchange objects.
	 */
	Collection<ExtendedExchange> filterExchanges(Predicate<ExtendedExchange> predicate){
		
		return Maps.filterValues(this.exchanges, predicate).values();
	}
	
	/**
	 * A much easier way to perform half the filtering/query functions needed on the
	 * ExtendedRouteInfo in-memory collection.
	 * @param predicate Predicate with the filter logic for ExtendedRouteInfo.
	 * @return A collection of matching ExtendedRouteInfo objects.
	 */
	Collection<ExtendedRouteInfo> filterRoutes(Predicate<ExtendedRouteInfo> predicate){
		
		return Maps.filterValues(this.routes, predicate).values();
	}

	@Override
	public Collection<Broker> getBrokers() {
		
		HashSet<Broker> brokers = new HashSet<Broker>();
		
		for (ExtendedExchange exchange : this.exchanges.values()){
			
			brokers.add(
				new Broker(
					exchange.getHostName(), exchange.getPort(), exchange.getVirtualHost()));
		}
		
		return brokers;
	}

	@Override
	public Collection<String> getTopics() {
		
		HashSet<String> topics = new HashSet<String>();
		
		for (ExtendedRouteInfo route : this.routes.values()){
			
			for (String topic : route.getTopics()){
			
				topics.add(topic);
			}
		}
		
		return topics;
	}

	@Override
	public Collection<String> getClients() {
		
		HashSet<String> clients = new HashSet<String>();
		
		for (ExtendedRouteInfo route : this.routes.values()){
			
			for (String client : route.getClients()){
			
				clients.add(client);
			}
		}
		
		return clients;
	}
}