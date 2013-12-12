package amp.rabbit.connection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import amp.rabbit.topology.BaseRoute;
import amp.rabbit.topology.Broker;

public class ConnectionManagerCache implements IConnectionManagerCache {

	protected Logger log;

	protected final Map<String, IRabbitConnectionFactory> _connectionFactories;
	
	protected final ConcurrentHashMap<Broker, IConnectionManager> pooledManagers =
			new ConcurrentHashMap<Broker, IConnectionManager>();

	public ConnectionManagerCache(Map<String, IRabbitConnectionFactory> connectionFactories){
		_connectionFactories = connectionFactories;
		log = LoggerFactory.getLogger(this.getClass());
	}
	
	public ConnectionManagerCache(IRabbitConnectionFactory connectionFactory){
		this(new HashMap<String, IRabbitConnectionFactory>());
		_connectionFactories.put("", connectionFactory);
	}
	
	public synchronized Collection<IConnectionManager> getConnectionManagersFor(BaseRoute route) throws Exception {
		
		Collection<IConnectionManager> managers = new ArrayList<IConnectionManager>();
		for (Broker broker : route.getBrokers()) {
			log.trace("Getting connection manager for broker: {}", broker);
			
			IConnectionManager manager = null;
			//TODO: Faster performance to just get manager and if null create?  Why lookup then get?
			if (pooledManagers.containsKey(broker)){
				
				manager = pooledManagers.get(broker);
			}
			else {
				
				String connectionStrategy = broker.getConnectionStrategy();
				IRabbitConnectionFactory connectionFactoryForStrategy = 
						_connectionFactories.get(connectionStrategy == null ? "" : connectionStrategy);
				
				if(connectionFactoryForStrategy == null){
					throw new Exception("No connection factory configured for strategy: '" + connectionStrategy + "'");
				} 
				
				manager = connectionFactoryForStrategy
						.getConnectionManagerFor(broker);
				
				pooledManagers.put(broker, manager);
			}
			managers.add(manager);
			//TODO: Fixing soon...
			//connection.addShutdownListener(new RabbitConnectionShutdownListener(this, exchange));
		}
		
		return managers;
	}

	/**
	 * Iterate over cached connections, closing each connection.
	 */
	@Override
	public void dispose() {
		
		for (IConnectionManager connection : this.pooledManagers.values()){
				
			connection.dispose();
				
		}
	}
}
