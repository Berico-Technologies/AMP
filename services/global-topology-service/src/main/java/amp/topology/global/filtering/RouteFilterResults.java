package amp.topology.global.filtering;

import amp.topology.global.Partition;
import amp.topology.global.impl.BasePartition;
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

    private final Collection<Partition> consumerPartitions;

    /**
     * Instantiate the object with applicable Topology constructs.
     * @param producerPartitions Applicable partitions to produce to.
     * @param consumerPartitions Applicable partitions to consume from.
     */
    public RouteFilterResults(
            Collection<Partition> producerPartitions,
            Collection<Partition> consumerPartitions) {

        this.producerPartitions = producerPartitions;
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

        public Builder(RouteRequirements requirements){
            this.requirements = requirements;
        }

        public Builder produceOn(Partition... producerPartitions){

            this.producerPartitions.addAll(Arrays.asList(producerPartitions));

            return this;
        }

        public Builder produceOn(Collection<? extends Partition> producerPartitions){

            this.producerPartitions.addAll(producerPartitions);

            return this;
        }

        public Builder consumeOn(Partition... consumerPartitions){

            this.consumerPartitions.addAll(Arrays.asList(consumerPartitions));

            return this;
        }

        public Builder consumeOn(Collection<? extends Partition> consumerPartitions){

            this.consumerPartitions.addAll(consumerPartitions);

            return this;
        }

        public RouteFilterResults build(){

            return new RouteFilterResults(producerPartitions, consumerPartitions);
        }
    }
}
