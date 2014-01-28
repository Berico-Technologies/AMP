package amp.topology.protocols.rabbit.topology;

import amp.rabbit.topology.ProducingRoute;
import amp.topology.protocols.rabbit.requirements.RabbitRouteRequirements;

/**
 * A BasePartition capable of providing a Producing Route.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public interface ProducingRouteProvider {

    ProducingRoute getProducingRoute(RabbitRouteRequirements requirements) throws Exception;
}
