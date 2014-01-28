package amp.topology.protocols.rabbit.topology;

import amp.rabbit.topology.ConsumingRoute;
import amp.topology.protocols.rabbit.requirements.RabbitRouteRequirements;

/**
 * A BasePartition capable of providing ConsumingRoutes based on requirements.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public interface ConsumingRouteProvider {

    ConsumingRoute getConsumingRoute(RabbitRouteRequirements requirements) throws Exception;
}
