package amp.topology.global.protocols;

import amp.rabbit.topology.RoutingInfo;
import amp.topology.global.filtering.RouteFilterResults;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public interface AmqpTopologyAdaptor {

    RoutingInfo adapt(RouteFilterResults filterResults);

}
