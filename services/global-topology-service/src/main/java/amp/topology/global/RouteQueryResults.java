package amp.topology.global;

import java.util.Collection;

/**
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class RouteQueryResults {

    private final Collection<ProducerGroup<? extends Partition>> producerGroups;

    private final Collection<Connector<? extends Partition, ? extends Partition>> connectors;

    private final Collection<ConsumerGroup<? extends Partition>> consumerGroups;

    public RouteQueryResults(
            Collection<ProducerGroup<? extends Partition>> producerGroups,
            Collection<Connector<? extends Partition, ? extends Partition>> connectors,
            Collection<ConsumerGroup<? extends Partition>> consumerGroups) {

        this.producerGroups = producerGroups;
        this.connectors = connectors;
        this.consumerGroups = consumerGroups;
    }

    public Collection<ProducerGroup<? extends Partition>> getProducerGroups() {
        return producerGroups;
    }

    public Collection<Connector<? extends Partition, ? extends Partition>> getConnectors() {
        return connectors;
    }

    public Collection<ConsumerGroup<? extends Partition>> getConsumerGroups() {
        return consumerGroups;
    }
}
