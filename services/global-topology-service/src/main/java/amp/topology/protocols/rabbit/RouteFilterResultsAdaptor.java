package amp.topology.protocols.rabbit;

import amp.rabbit.topology.ConsumingRoute;
import amp.rabbit.topology.ProducingRoute;
import amp.rabbit.topology.RoutingInfo;
import amp.topology.global.Partition;
import amp.topology.global.filtering.RouteFilterResults;
import amp.topology.protocols.rabbit.requirements.RabbitRouteRequirements;
import amp.topology.protocols.rabbit.topology.ConsumingRouteProvider;
import amp.topology.protocols.rabbit.topology.ProducingRouteProvider;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Utilities for converting the results returned from a Route Filter into RoutingInfo
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class RouteFilterResultsAdaptor {

    /**
     * Convert RouteFilterResults into Routing info.
     * @param routeResults Results to convert
     * @param routeRequirements Original Filtering Requirements
     * @return RoutingInfo
     * @throws Exception thrown if there's an issue getting the producer or consumer route
     * from the underlying partitions.
     */
    public static RoutingInfo convert(
            RouteFilterResults routeResults,
            RabbitRouteRequirements routeRequirements) throws Exception {

        Collection<ProducingRoute> producingRoutes = convertProducingRoutes(routeResults, routeRequirements);

        Collection<ConsumingRoute> consumingRoutes = convertConsumingRoutes(routeResults, routeRequirements);

        return new RoutingInfo(producingRoutes, consumingRoutes);
    }

    /**
     * Convert RouteFilterResults into ProducingRoutes.
     * @param routeResults Results to convert
     * @param routeRequirements Original Filtering Requirements
     * @return Collection of ProducingRoutes
     * @throws Exception thrown if there's an issue getting the producer route from the underlying
     * partitions.
     */
    public static Collection<ProducingRoute> convertProducingRoutes(
            RouteFilterResults routeResults,
            RabbitRouteRequirements routeRequirements) throws Exception {

        ArrayList<ProducingRoute> producingRoutes = Lists.newArrayList();

        for (Partition p : routeResults.getProducerPartitions()){

            if (ProducingRouteProvider.class.isAssignableFrom(p.getClass())){

                ProducingRouteProvider provider = (ProducingRouteProvider)p;

                producingRoutes.add(provider.getProducingRoute(routeRequirements));
            }
        }

        return producingRoutes;
    }

    /**
     * Convert the RouteFilterResults into ConsumingRoutes.
     * @param routeResults Results to convert
     * @param routeRequirements Original Filtering Requirements
     * @return Collection of ConsumingRoutes
     * @throws Exception thrown if there's an issue getting the consumer route from the underlying
     * partitions.
     */
    public static Collection<ConsumingRoute> convertConsumingRoutes(
            RouteFilterResults routeResults,
            RabbitRouteRequirements routeRequirements) throws Exception {

        ArrayList<ConsumingRoute> consumingRoutes = Lists.newArrayList();

        for (Partition p : routeResults.getConsumerPartitions()){

            if (ConsumingRouteProvider.class.isAssignableFrom(p.getClass())){

                ConsumingRouteProvider provider = (ConsumingRouteProvider)p;

                consumingRoutes.add(provider.getConsumingRoute(routeRequirements));
            }
        }

        return consumingRoutes;
    }
}
