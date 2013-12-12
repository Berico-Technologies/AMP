package amp.rabbit.topology;


import java.util.Map;

import cmf.bus.IDisposable;


/**
 * Describes the requirements of a topology service, a mechanism
 * used to decide where messages should be produced or consumed
 * by the RabbitTransportProvider.
 * 
 * @author John Ruiz (Berico Technologies)
 */
public interface ITopologyService extends IDisposable {

	/**
	 * Given some context (Envelope Headers or Registration Info),
	 * provide the necessary routing information.
	 * 
	 * @param routingHints Context for Routing, usually the headers
	 * from an Envelope or info from an IRegistration.
	 * @return Producing and Consuming information for a particular context.
	 */
    RoutingInfo getRoutingInfo(Map<String, String> routingHints);
}
