package amp.topology.protocols.rabbit.topology;

import amp.topology.global.Connector;
import amp.topology.global.ConsumerGroup;
import amp.topology.global.ProducerGroup;
import amp.topology.global.impl.BaseConnector;
import amp.topology.protocols.rabbit.management.Cluster;
import amp.topology.protocols.rabbit.topology.exceptions.GroupHasNoPartitionsException;import amp.topology.protocols.rabbit.topology.exceptions.PartitionOnForeignClusterException;import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * This is a simple implementation of a Connector.  It's purpose is merely to synchronize routing keys
 * between producers and consumers and to ensure
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class RabbitInterVHostConnector extends BaseConnector<RabbitProducerPartition, RabbitConsumerPartition> {

    final Cluster cluster;

    final Set<String> routingKeys = Sets.newCopyOnWriteArraySet();

    public RabbitInterVHostConnector(
            String description,
            ProducerGroup<RabbitProducerPartition> producerGroup,
            ConsumerGroup<RabbitConsumerPartition> consumerGroup,
            Cluster cluster,
            Collection<String> routingKeys) {

        super(description, producerGroup, consumerGroup);

        this.cluster = cluster;
        this.routingKeys.addAll(routingKeys);
    }

    public RabbitInterVHostConnector(
            String id,
            String description,
            ProducerGroup<RabbitProducerPartition> producerGroup,
            ConsumerGroup<RabbitConsumerPartition> consumerGroup,
            Cluster cluster,
            Collection<String> routingKeys) {

        super(id, description, producerGroup, consumerGroup);

        this.cluster = cluster;
        this.routingKeys.addAll(routingKeys);
    }

    /**
     * Verify the state of this connector.
     */
    @Override
    public void verify() throws Exception {

        Collection<BaseRabbitPartition> producingPartitions = this.getProducerGroup().getPartitions();

        validatePartitions(producingPartitions);

        Collection<BaseRabbitPartition> consumingPartitions = this.getConsumerGroup().getPartitions();

        validatePartitions(consumingPartitions);

        setState(ConnectorStates.ACTIVE, "Everything checks out!");
    }

    /**
     * Validate the state of the partitions in either the Producer or Consumer Group.
     * @param partitions The partitions to validate.
     */
    void validatePartitions(Collection<BaseRabbitPartition> partitions) throws Exception {

        checkHasPartitions(partitions);

        checkOnSameCluster(cluster, partitions);

        checkHasRoutingKeys(partitions, routingKeys);
    }

    void checkHasPartitions(Collection<BaseRabbitPartition> partitions) throws GroupHasNoPartitionsException {

        if (partitions.size() == 0){

            setState(ConnectorStates.IN_ERROR, "There are no partitions for the ProducerGroup.");

            throw new GroupHasNoPartitionsException(this.getId());
        }
    }

    void checkOnSameCluster(Cluster cluster, Collection<BaseRabbitPartition> partitions)
            throws PartitionOnForeignClusterException {

        for (BaseRabbitPartition partition : partitions){

            checkOnSameCluster(cluster, partition);
        }
    }

    void checkOnSameCluster(Cluster cluster, BaseRabbitPartition partition) throws PartitionOnForeignClusterException {

        if (!cluster.equals(partition.getCluster())){

            setState(ConnectorStates.IN_ERROR, String.format(
                    "Connector can only bridge groups on the same cluster/virtual host.  " +
                            "Partition '%s' exists on cluster '%s' and first partition is on '%s'.",
                    partition.getId(),
                    partition.getCluster().getClusterId(),
                    cluster.getClusterId()));

            throw new PartitionOnForeignClusterException(cluster, partition);
        }
    }

    void checkHasRoutingKeys(Collection<BaseRabbitPartition> partitions, Collection<String> routingKeys){

        for (BaseRabbitPartition partition : partitions){

            if (Collections.disjoint(partition.getRoutingKeys(), routingKeys)){

                partition.getRoutingKeys().clear();

                // Fix the inconsistent synchronization if there is one
                partition.getRoutingKeys().addAll(routingKeys);
            }
        }
    }

    /**
     * Add an alias (routing key) between the Producing and Consuming Group.
     * @param routingAlias Alias to add.
     * @throws Exception Thrown if the verification process fails.
     */
    public void addAlias(String routingAlias) throws Exception {

        Preconditions.checkNotNull(routingAlias);

        this.routingKeys.add(routingAlias);

        verify();
    }

    /**
     * Remove an alias (routing key) between the Producing and Consuming Group.
     * @param routingAlias Alias to remove.
     * @throws Exception Thrown if the verification process fails.
     */
    public void removeAlias(String routingAlias) throws Exception {

        Preconditions.checkNotNull(routingAlias);

        this.routingKeys.remove(routingAlias);

        verify();
    }

    @Override
    public void activate() throws Exception {

        verify();
    }

    @Override
    public void deactivate() throws Exception {}

    @Override
    public void setup() throws Exception {

        verify();
    }

    @Override
    public void cleanup() throws Exception {}
}
