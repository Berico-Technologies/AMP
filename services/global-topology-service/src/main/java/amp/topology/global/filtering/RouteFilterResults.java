package amp.topology.global.filtering;

import amp.topology.global.*;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Container for all the applicable TopologyGroups and Connectors based on RouteRequirements.
 *
 * It's up the requester to interpret those results into lower level topology.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class RouteFilterResults {

    private final Collection<ProducerGroup<? extends Partition>> producerGroups;

    private final Collection<Connector<? extends Partition, ? extends Partition>> connectors;

    private final Collection<ConsumerGroup<? extends Partition>> consumerGroups;

    private final RouteRequirements requirements;

    /**
     * Instantiate the object with applicable Topology constructs.
     * @param requirements Underlying requirements for the Route (used to return partitions).
     * @param producerGroups Applicable Producer Groups.
     * @param connectors Applicable Connectors.
     * @param consumerGroups Applicable Consumer Groups.
     */
    public RouteFilterResults(
            RouteRequirements requirements,
            Collection<ProducerGroup<? extends Partition>> producerGroups,
            Collection<Connector<? extends Partition, ? extends Partition>> connectors,
            Collection<ConsumerGroup<? extends Partition>> consumerGroups) {

        this.requirements = requirements;
        this.producerGroups = producerGroups;
        this.connectors = connectors;
        this.consumerGroups = consumerGroups;
    }

    /**
     * Applicable Producer Groups.
     * @return Collection of ProducerGroups
     */
    public Collection<ProducerGroup<? extends Partition>> getProducerGroups() {
        return producerGroups;
    }

    /**
     * Get the applicable Producer Partitions.
     * @return Producer Partitions
     */
    public Collection<? extends Partition> getProducerPartitions(){

        ArrayList<Partition> partitions = Lists.newArrayList();

        for (ProducerGroup<? extends Partition> group : producerGroups){

            Collection<? extends Partition> applicablePartitions = group.filter(requirements);

            if (applicablePartitions != null && applicablePartitions.size() > 0){

                partitions.addAll(applicablePartitions);
            }
        }

        return partitions;
    }

    /**
     * Applicable Connectors.
     * @return Collection of Connectors.
     */
    public Collection<Connector<? extends Partition, ? extends Partition>> getConnectors() {
        return connectors;
    }

    /**
     * Applicable Consumer Groups.
     * @return Collection of Consumer Groups.
     */
    public Collection<ConsumerGroup<? extends Partition>> getConsumerGroups() {
        return consumerGroups;
    }

    /**
     * Get the applicable Partitions.
     * @return Collection of Consumer Partitions
     */
    public Collection<? extends Partition> getConsumerPartitions(){

        ArrayList<Partition> partitions = Lists.newArrayList();

        for (ConsumerGroup<? extends Partition> group : consumerGroups){

            Collection<? extends Partition> applicablePartitions = group.filter(requirements);

            if (applicablePartitions != null && applicablePartitions.size() > 0){

                partitions.addAll(applicablePartitions);
            }
        }

        return partitions;
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
                    null,
                    new ArrayList<ProducerGroup<? extends Partition>>(),
                    new ArrayList<Connector<? extends Partition, ? extends Partition>>(),
                    new ArrayList<ConsumerGroup<? extends Partition>>());
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

        ArrayList<ProducerGroup<? extends Partition>> pgroups = Lists.newArrayList();

        ArrayList<ConsumerGroup<? extends Partition>> cgroups = Lists.newArrayList();

        ArrayList<Connector<? extends Partition, ? extends Partition>> connectors = Lists.newArrayList();

        public Builder(RouteRequirements requirements){
            this.requirements = requirements;
        }

        public Builder pgroups(ProducerGroup<? extends Partition>... pgroups){

            this.pgroups.addAll(Arrays.asList(pgroups));

            return this;
        }

        public Builder cgroups(ConsumerGroup<? extends Partition>... cgroups){

            this.cgroups.addAll(Arrays.asList(cgroups));

            return this;
        }

        public Builder connectors(Connector<? extends Partition, ? extends Partition>... connectors){

            this.connectors.addAll(Arrays.asList(connectors));

            return this;
        }

        public RouteFilterResults build(){

            return new RouteFilterResults(requirements, pgroups, connectors, cgroups);
        }
    }
}
