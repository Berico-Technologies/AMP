package amp.topology.global.filtering;

import amp.topology.global.*;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Container for all the applicable Partitions and Connectors based on RouteRequirements.
 *
 * It's up the requester to interpret those results into lower level topology.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class RouteFilterResults {

    private final Collection<Partition> producerPartitions;

    private final Collection<Connector<? extends Partition, ? extends Partition>> connectors;

    private final Collection<Partition> consumerPartitions;

    /**
     * Instantiate the object with applicable Topology constructs.
     * @param producerPartitions Applicable partitions to produce to.
     * @param connectors Applicable Connectors.
     * @param consumerPartitions Applicable partitions to consume from.
     */
    public RouteFilterResults(
            Collection<Partition> producerPartitions,
            Collection<Connector<? extends Partition, ? extends Partition>> connectors,
            Collection<Partition> consumerPartitions) {

        this.producerPartitions = producerPartitions;
        this.connectors = connectors;
        this.consumerPartitions = consumerPartitions;
    }

    /**
     * Get the Partitions to produce on.
     * @return Partitions to produce on.
     */
    public Collection<Partition> getProducerPartitions() {

        return producerPartitions;
    }

    /**
     * Get the Partitions to consume from.
     * @return Partitions to consume from.
     */
    public Collection<Partition> getConsumerPartitions() {
        return consumerPartitions;
    }

    /**
     * Applicable Connectors.
     * @return Collection of Connectors.
     */
    public Collection<Connector<? extends Partition, ? extends Partition>> getConnectors() {
        return connectors;
    }

    /**
     * EMPTY instance, for which you can perform comparisons.
     */
    public static final EmptyRouteFilterResults EMPTY = new EmptyRouteFilterResults();

    /**
     * A little sugar to make empty results easier.
     * @author Richard Clayton (Berico Technologies)
     */
    public static class EmptyRouteFilterResults extends RouteFilterResults {

        /**
         * Instantiate the object with applicable Topology constructs.
         */
        public EmptyRouteFilterResults() {
            super(
                    new ArrayList<Partition>(),
                    new ArrayList<Connector<? extends Partition, ? extends Partition>>(),
                    new ArrayList<Partition>());
        }
    }

    /**
     * Get a builder for this monster.
     * @return A builder instance.
     */
    public static Builder Builder(RouteRequirements requirements){

        return new Builder(requirements);
    }

    /**
     * Simplifies working with the ghetto fab complex, final collections needed by the RouteFilterResults class.
     */
    public static class Builder {

        RouteRequirements requirements;

        ArrayList<Partition> producerPartitions = Lists.newArrayList();

        ArrayList<Partition> consumerPartitions = Lists.newArrayList();

        ArrayList<Connector<? extends Partition, ? extends Partition>> connectors = Lists.newArrayList();

        public Builder(RouteRequirements requirements){
            this.requirements = requirements;
        }

        public Builder produceOn(Partition... producerPartitions){

            this.producerPartitions.addAll(Arrays.asList(producerPartitions));

            return this;
        }

        public Builder consumeOn(Partition... consumerPartitions){

            this.consumerPartitions.addAll(Arrays.asList(consumerPartitions));

            return this;
        }

        public Builder connectBy(Connector<? extends Partition, ? extends Partition>... connectors){

            this.connectors.addAll(Arrays.asList(connectors));

            return this;
        }

        public RouteFilterResults build(){

            return new RouteFilterResults(producerPartitions, connectors, consumerPartitions);
        }
    }
}
