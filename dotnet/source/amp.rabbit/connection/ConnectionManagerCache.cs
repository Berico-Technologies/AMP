using System;
using System.Collections.Generic;
using amp.rabbit.topology;
using Common.Logging;

namespace amp.rabbit.connection
{
    public class ConnectionManagerCache : IConnectionManagerCache
    {
        protected ILog _log;

	    protected readonly IDictionary<String, IRabbitConnectionFactory> _connectionFactories;
	
	    protected readonly IDictionary<Broker, IConnectionManager> _pooledManagers =
			    new Dictionary<Broker, IConnectionManager>();

	    public ConnectionManagerCache(IDictionary<String, IRabbitConnectionFactory> connectionFactories)
        {
		    _connectionFactories = connectionFactories;
		    _log =  LogManager.GetLogger(this.GetType());
	    }
	
	    public ConnectionManagerCache(IRabbitConnectionFactory connectionFactory)
		    :this(new Dictionary<String, IRabbitConnectionFactory>())
        {
		    _connectionFactories.Add("", connectionFactory);
	    }
	
	    public IEnumerable<IConnectionManager> GetConnectionManagersFor(BaseRoute route)
        {
		
		    List<IConnectionManager> managers = new List<IConnectionManager>();

	        foreach (Broker broker in route.Brokers) {
			    _log.Trace(string.Format("Getting connection manager for broker: {0}", broker));
			
			    IConnectionManager manager = null;
			    if(!_pooledManagers.TryGetValue(broker, out manager))
                {
				    String connectionStrategy = broker.ConnectionStrategy;
				    IRabbitConnectionFactory connectionFactoryForStrategy = null;
                    if(!_connectionFactories.TryGetValue(connectionStrategy ?? "", out connectionFactoryForStrategy))
                    {
					    throw new Exception("No connection factory configured for strategy: '" + connectionStrategy + "'");
				    } 
				
				    manager = connectionFactoryForStrategy.ConnectTo(broker);
				
				    _pooledManagers.Add(broker, manager);
			    }
			    managers.Add(manager);
			    //TODO: Fixing soon...
			    //connection.addShutdownListener(new RabbitConnectionShutdownListener(this, exchange));
		    }
		
		    return managers;
	    }

        ///<summary>
        /// Iterate over cached connections, closing each connection.
        ///</summary>
	    public void Dispose() 
        {
		    foreach (IConnectionManager connection in _pooledManagers.Values)
            {
			    connection.Dispose();	
		    }
	    }
    }
}
