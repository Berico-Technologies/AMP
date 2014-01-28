package amp.topology.global.filtering;

import amp.topology.anubis.Actor;

import java.util.Map;

/**
 * Essentially designed to wrap Routing Info, with explicit accessors to
 * key topology information.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public interface RouteRequirements extends Map<String, String> {

    /**
     * Get the BaseTopic in which route information is desired.
     * @return BaseTopic (amp.my.BaseTopic)
     */
    String getTopic();

    /**
     * Get the Protocol requested.
     *
     * Note:  In future versions this may be NULL, in part because we are hoping
     * that the GTS will be able to tell the client what protocol to use
     * (potentially shipping dependencies [JAR, DLL] to the client).
     *
     * @return Protocol (AMQP, WebStomp)
     */
    String getProtocol();

    /**
     * Optionally, the pattern in which the Client is attempting to use.  The default
     * is PubSub, but it may be ScatterGather or RPC.  This allows TopologyGroups to selectively
     * decide whether they are best suited for a particular messaging task.
     *
     * @return Pattern (PubSub, RPC, ScatterGather)
     */
    String getMessagePattern();

    /**
     * It's unlikely that the requirements will have the original actor.
     * @param actor Actor making the request.
     */
    void setActor(Actor actor);

    /**
     * The secure identity of the client.
     * @return The identity of the client.
     */
    Actor getActor();

    /**
     * RouteDirections of the Route (are you Publishing or Consuming)
     */
    public enum RouteDirections {
        PUBLISH,
        CONSUME
    }

    /**
     * Is this a Publish or Consume?
     * @return Direction of route.
     */
    RouteDirections getRouteDirection();
}
