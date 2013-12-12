package amp.topology.core;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides essential functionality, particularly around handling the Event Listeners
 * implementing ITopologyRepositories would need if they had to implement from scratch.
 * 
 * @author Richard Clayton (Berico Technologies)
 */
public abstract class BaseTopologyRepository implements ITopologyRepository {

	private static final Logger logger = LoggerFactory.getLogger(BaseTopologyRepository.class);
	
	BlockingQueue<ITopologyRepositoryEventListener> listeners = 
			new LinkedBlockingQueue<ITopologyRepositoryEventListener>();
	
	public BaseTopologyRepository(){}
	
	public BaseTopologyRepository(Collection<ITopologyRepositoryEventListener> listeners){
		
		setEventListeners(listeners);
	}
	
	public void setEventListeners(Collection<ITopologyRepositoryEventListener> listeners){
		
		logger.debug("Adding listeners {}", listeners);
		
		this.listeners.addAll(listeners);
	}
	
	@Override
	public void addEventListener(ITopologyRepositoryEventListener listener) {
		
		logger.debug("Adding listener {}", listener);
		
		listeners.add(listener);
	}

	@Override
	public void removeEventListener(ITopologyRepositoryEventListener listener) {
		
		logger.debug("Removing listener {}", listener);
		
		listeners.remove(listener);
	}
	
	/* ############# Event Triggers ############################################ */
	
	protected void fireRouteCreated(ExtendedRouteInfo routeInfo){
		
		logger.debug("fireRouteCreated({})", routeInfo);
		
		for (ITopologyRepositoryEventListener listener : listeners){
			
			listener.routeCreated(routeInfo);
		}
	}
	
	protected void fireRouteRemoved(ExtendedRouteInfo routeInfo){
		
		logger.debug("fireRouteRemoved({})", routeInfo);
		
		for (ITopologyRepositoryEventListener listener : listeners){
			
			listener.routeRemoved(routeInfo);
		}
	}
	
	protected void fireRouteUpdated(ExtendedRouteInfo oldRouteInfo, ExtendedRouteInfo newRouteInfo){
		
		logger.debug("fireRouteUpdated({}, {})", oldRouteInfo, newRouteInfo);
		
		for (ITopologyRepositoryEventListener listener : listeners){
			
			listener.routeUpdated(oldRouteInfo, newRouteInfo);
		}
	}
	
	protected void fireExchangeCreated(ExtendedExchange exchange){
		
		logger.debug("fireExchangeCreated({})", exchange);
		
		for (ITopologyRepositoryEventListener listener : listeners){
			
			listener.exchangeCreated(exchange);
		}
	}
	
	protected void fireExchangeRemoved(ExtendedExchange exchange){
		
		logger.debug("fireExchangeRemoved({})", exchange);
		
		for (ITopologyRepositoryEventListener listener : listeners){
			
			listener.exchangeRemoved(exchange);
		}
	}

	protected void fireExchangeUpdated(ExtendedExchange oldExchange, ExtendedExchange newExchange){
		
		logger.debug("fireExchangeUpdated({}, {})", oldExchange, newExchange);
		
		for (ITopologyRepositoryEventListener listener : listeners){
			
			listener.exchangeUpdated(oldExchange, newExchange);
		}
	}
	
	protected void fireRoutingInfoRetrieved(String topic, String client, Collection<ExtendedRouteInfo> routingInfo){
		
		logger.debug("fireRoutingInfoRetrieved({}, {}, has routes: {})", 
				new Object[]{ topic, client, routingInfo.size() > 0 });
		
		for (ITopologyRepositoryEventListener listener : listeners){
			
			listener.routingInfoRetrieved(topic, client, routingInfo);
		}
	}
}
