package amp.topology.protocols.rabbit.topology;

import amp.rabbit.topology.Exchange;
import amp.rabbit.topology.ProducingRoute;
import amp.topology.protocols.rabbit.management.Cluster;
import amp.topology.protocols.rabbit.requirements.RabbitRouteRequirements;

import java.util.Collection;
import java.util.Map;

/**
 * Basic Producing BasePartition representing a RabbitMQ Exchange.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class RabbitProducerPartition extends BaseRabbitPartition implements ProducingRouteProvider {

    /**
     * Initialize the partition with the Cluster, Exchange, and Routing Key Info.
     *
     * @param cluster     the Cluster in which the Exchange should exist.
     * @param exchange    Exchange configuration.
     * @param routingKeys Routing Keys for producing or consuming on the exchange.
     */
    public RabbitProducerPartition(Cluster cluster, Exchange exchange, Collection<String> routingKeys) {
        super(cluster, exchange, routingKeys);
    }

    /**
     * Create a ProducingRoute from the underlying BasePartition configuration.
     * @param requirements Request requirements, in this BasePartition's case, it is ignored
     *                     since their are no applicable request-specific configuration
     *                     for this partition.
     * @return ProducingRoute
     */
    @Override
    public ProducingRoute getProducingRoute(RabbitRouteRequirements requirements) {

        return ProducingRoute
                .builder()
                .exchange(getExchange())
                .routingKeys(getRoutingKeys())
                .brokers(getBrokers())
                .build();
    }

    //TODO: Finish
    @Override
    public void set(Map<String, String> properties) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<String, String> getExtensionProperties() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
