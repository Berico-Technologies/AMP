package amp.topology.protocols.webstomp.requirements;

import amp.topology.global.filtering.RouteRequirements;

/**
 * Specific configuration requirements of a WebStomp client.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public interface WebStompRouteRequirements extends RouteRequirements {

    /**
     * Should the underlying components create the topology objects for
     * the client?
     * @return TRUE if the client wants those components created for them.
     */
    boolean shouldCreateTopology();

    /**
     * Desired Queue Name
     * @return The desired name of the Queue.
     */
    String queueName();

    /**
     * Desired Queue Prefix
     * @return The desired prefix for a queue, if the name is uniquely generated.
     */
    String queuePrefix();
}
