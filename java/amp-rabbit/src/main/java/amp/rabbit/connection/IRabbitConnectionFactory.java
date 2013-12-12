package amp.rabbit.connection;

import amp.rabbit.topology.Broker;

/**
 * Factory that creates the managers for connection+channel generation 
 * to the various route(s) needed by the client objects.
 *
 * @author jmccune
 */
public interface IRabbitConnectionFactory {

	IConnectionManager getConnectionManagerFor(Broker broker) throws Exception;
}
