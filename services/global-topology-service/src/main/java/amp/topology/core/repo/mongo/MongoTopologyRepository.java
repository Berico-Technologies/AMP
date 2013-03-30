package amp.topology.core.repo.mongo;

import java.util.Collection;
import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;

import amp.topology.core.BaseTopologyRepository;
import amp.topology.core.ExtendedExchange;
import amp.topology.core.ExtendedRouteInfo;
import amp.topology.core.ITopologyRepository;
import amp.topology.core.ITopologyRepositoryEventListener;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * MongoDB implementation of the Topology Repository.  This is useful for
 * a production system that needs to not only persist the Topology state,
 * but also allow multiple Global Topology Service instances to exist
 * for load balancing purposes.
 * 
 * @author Richard Clayton (Berico Technologies)
 */
public class MongoTopologyRepository extends BaseTopologyRepository implements ITopologyRepository {

	protected MongoTemplate mongoTemplate;
	
	public MongoTopologyRepository(MongoTemplate mongoTemplate) {
		
		super();
		
		this.mongoTemplate = mongoTemplate;
	}

	public MongoTopologyRepository(
			Collection<ITopologyRepositoryEventListener> listeners,
			MongoTemplate mongoTemplate) {
		
		super(listeners);
		
		this.mongoTemplate = mongoTemplate;
	}
	
	@Override
	public ExtendedExchange getExchange(String id) {
		
		return mongoTemplate.findById(id, ExtendedExchange.class);
	}

	@Override
	public ExtendedRouteInfo getRoute(String id) {
		
		return mongoTemplate.findById(id, ExtendedRouteInfo.class);
	}

	@Override
	public List<ExtendedExchange> getExchanges() {
		
		return mongoTemplate.findAll(ExtendedExchange.class);
	}

	@Override
	public List<ExtendedRouteInfo> getRoutes() {
		
		return mongoTemplate.findAll(ExtendedRouteInfo.class);
	}

	@Override
	public void createExchange(ExtendedExchange exchange) {
		
		mongoTemplate.insert(exchange);
		
		fireExchangeCreated(exchange);
	}

	@Override
	public void createRoute(ExtendedRouteInfo routeInfo) {
		
		mongoTemplate.insert(routeInfo);
		
		fireRouteCreated(routeInfo);
	}

	@Override
	public void updateExchange(ExtendedExchange exchange) {
		
		ExtendedExchange oldExchange = getExchange(exchange.getId());
		
		mongoTemplate.save(exchange);
		
		fireExchangeUpdated(oldExchange, exchange);
	}

	@Override
	public void updateRoute(ExtendedRouteInfo routeInfo) {
		
		ExtendedRouteInfo oldRouteInfo = getRoute(routeInfo.getId());
		
		mongoTemplate.save(routeInfo);
		
		fireRouteUpdated(oldRouteInfo, routeInfo);
	}
	
	@Override
	public boolean removeExchange(String id) {
		
		ExtendedExchange exchange = 
			mongoTemplate.findAndRemove(query(where("id").is(id)), ExtendedExchange.class);

		fireExchangeRemoved(exchange);
		
		return true;
	}

	@Override
	public boolean removeRoute(String id) {
		
		ExtendedRouteInfo routeInfo = 
			mongoTemplate.findAndRemove(query(where("id").is(id)), ExtendedRouteInfo.class);

		fireRouteRemoved(routeInfo);
		
		return true;
	}

	@Override
	public List<ExtendedRouteInfo> find(String topic, String client) {
		
		List<ExtendedRouteInfo> routingInfo = mongoTemplate.find(
				query(where("topics").is(topic).and("clients").is(client)), 
					ExtendedRouteInfo.class);
		
		fireRoutingInfoRetrieved(topic, client, routingInfo);
		
		return routingInfo;
	}

	@Override
	public void purge() {
		
		mongoTemplate.dropCollection(ExtendedExchange.class);
		mongoTemplate.dropCollection(ExtendedRouteInfo.class);
	}

	@Override
	public List<ExtendedExchange> getExchangesByBroker(String host) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ExtendedExchange> getExchangesByBroker(String host, int port) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ExtendedExchange> getExchangesByBroker(String host, int port,
			String vhost) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ExtendedRouteInfo> getRoutesByTopic(String topic) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ExtendedRouteInfo> getRoutesByClient(String client) {
		// TODO Auto-generated method stub
		return null;
	}

}
