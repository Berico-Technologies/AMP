package amp.topology.protocols.rabbit.requirements;

import amp.topology.global.filtering.RouteRequirements;

/**
 * A marker to differentiate Rabbit-based Route Requirements from other
 * RouteRequirements implementations.  This exists primarily to differentiate
 * the type of Jersey MessageBodyReader to use.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public interface RabbitRouteRequirements extends RouteRequirements {}
