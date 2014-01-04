package amp.topology.protocols.rabbit.topology;

import amp.rabbit.topology.ProducingRoute;
import amp.topology.global.Partition;
import amp.topology.protocols.rabbit.requirements.RabbitRouteRequirements;

/**
 * A Partition capable of providing a Producing Route.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public interface ProducingRouteProvider extends Partition {

    ProducingRoute getProducingRoute(RabbitRouteRequirements requirements) throws Exception;
}