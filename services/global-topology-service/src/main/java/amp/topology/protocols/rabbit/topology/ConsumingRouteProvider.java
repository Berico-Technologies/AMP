package amp.topology.protocols.rabbit.topology;

import amp.rabbit.topology.ConsumingRoute;
import amp.topology.global.Partition;
import amp.topology.protocols.rabbit.requirements.RabbitRouteRequirements;

/**
 * A Partition capable of providing ConsumingRoutes based on requirements.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public interface ConsumingRouteProvider extends Partition {

    ConsumingRoute getConsumingRoute(RabbitRouteRequirements requirements) throws Exception;
}
