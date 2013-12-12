package amp.rabbit.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import amp.rabbit.topology.Broker;

import com.rabbitmq.client.ConnectionFactory;

/**
 * Provides a lot of the boiler-plate functionality most Rabbit Connection Factories
 * would need to function properly. This base implementation imposes only one responsibility
 * on deriving classes: that they create and supply the connection to the broker (which
 * is where most configuration like authentication is done). 
 *  
 * @author Richard Clayton (Berico Technologies)
 * @author jmccune (Berico Technologies)
 *
 */
public abstract class BaseConnectionFactory implements IRabbitConnectionFactory {

    protected Logger log;
	/**
	 * Create a new instance of the ConnectionFactory using the "SameBrokerStrategy"
	 */
	public BaseConnectionFactory() {
        log = LoggerFactory.getLogger(this.getClass());
	}

	/**
	 * Derived classes have the sole responsibility of configuring the RabbitConnectionFactory.
	 * (however that is done: username/password, certificates, etc.).
	 */
	protected void configureConnectionFactory(ConnectionFactory factory, Broker broker) 
			throws Exception {

        factory.setHost(broker.getHostname());
        factory.setPort(broker.getPort());
        factory.setVirtualHost(broker.getVirtualHost());
	}

	/**
	 * Convenience method for DRY principle -- absolutely no difference between how we 
	 * create a connection for a ConsumingRoute or a ProducingRoute.  Just use the BaseRoute.
	 * @param broker
	 * @return
	 * @throws Exception
	 */
	public IConnectionManager getConnectionManagerFor(Broker broker) throws Exception {
		
        log.debug("Creating connection manager for broker: {}", broker);

		ConnectionFactory factory = new ConnectionFactory();

		configureConnectionFactory(factory, broker);
		
        return new ConnectionManager(factory);
	}
}
