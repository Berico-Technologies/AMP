package amp.topology.protocols.rabbit.topology;

import amp.topology.global.impl.BaseConnector;
import amp.topology.global.impl.BaseProducerGroup;
import amp.topology.global.impl.BaseConsumerGroup;
import amp.topology.protocols.rabbit.management.Cluster;
import amp.topology.protocols.rabbit.topology.exceptions.GroupHasNoPartitionsException;import amp.topology.protocols.rabbit.topology.exceptions.PartitionOnForeignClusterException;import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This is a simple implementation of a BaseConnector.  It's purpose is merely to synchronize routing keys
 * between producers and consumers and to ensure
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class RabbitInterVHostConnector extends BaseConnector<RabbitProducerPartition, RabbitConsumerPartition> {

    final Cluster cluster;

    final Set<String> routingKeys = Sets.newCopyOnWriteArraySet();

    public RabbitInterVHostConnector(
            String topicId,
            String description,
            BaseProducerGroup<RabbitProducerPartition> producerGroup,
            BaseConsumerGroup<RabbitConsumerPartition> consumerGroup,
            Cluster cluster,
            Collection<String> routingKeys) {

        super(topicId, description, producerGroup, consumerGroup);

        this.cluster = cluster;
        this.routingKeys.addAll(routingKeys);
    }

    public RabbitInterVHostConnector(
            String topicId,
            String connectorId,
            String description,
            BaseProducerGroup<RabbitProducerPartition> producerGroup,
            BaseConsumerGroup<RabbitConsumerPartition> consumerGroup,
            Cluster cluster,
            Collection<String> routingKeys) {

        super(topicId, description, producerGroup, consumerGroup);

        setConnectorId(connectorId);

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
     * Validate the state of the partitions in either the Producer or Consumer BaseGroup.
     * @param partitions The partitions to validate.
     */
    void validatePartitions(Collection<BaseRabbitPartition> partitions) throws Exception {

        checkHasPartitions(partitions);

        checkOnSameCluster(cluster, partitions);

        checkHasRoutingKeys(partitions, routingKeys);
    }

    void checkHasPartitions(Collection<BaseRabbitPartition> partitions) throws GroupHasNoPartitionsException {

        if (partitions.size() == 0){

            setState(ConnectorStates.IN_ERROR, "There are no partitions for the BaseProducerGroup.");

            throw new GroupHasNoPartitionsException(this.getConnectorId());
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
                    "BaseConnector can only bridge groups on the same cluster/virtual host.  " +
                            "BasePartition '%s' exists on cluster '%s' and first partition is on '%s'.",
                    partition.getPartitionId(),
                    partition.getCluster().getClusterId(),
                    cluster.getClusterId()));

            throw new PartitionOnForeignClusterException(cluster, partition);
        }
    }

    void checkHasRoutingKeys(Collection<BaseRabbitPartition> partitions, Collection<String> routingKeys){

        for (BaseRabbitPartition partition : partitions){

            checkHasRoutingKeys(partition, routingKeys);
        }
    }

    void checkHasRoutingKeys(BaseRabbitPartition partition, Collection<String> routingKeys){

        if (!  (partition.getRoutingKeys().containsAll(routingKeys)
                && routingKeys.containsAll(partition.getRoutingKeys()) )){

            Set<String> difference = new HashSet<String>(partition.getRoutingKeys());

            difference.removeAll(routingKeys);

            for (String unknownAlias : difference)
                partition.removeRoutingKey(unknownAlias);

            // Fix the inconsistent synchronization if there is one
            for (String alias : routingKeys)
                partition.addRoutingKey(alias);
        }
    }

    /**
     * Add an alias (routing key) between the Producing and Consuming BaseGroup.
     * @param routingAlias Alias to add.
     * @throws Exception Thrown if the verification process fails.
     */
    public void addAlias(String routingAlias) throws Exception {

        Preconditions.checkNotNull(routingAlias);

        this.routingKeys.add(routingAlias);

        verify();
    }

    /**
     * Remove an alias (routing key) between the Producing and Consuming BaseGroup.
     * @param routingAlias Alias to remove.
     * @throws Exception Thrown if the verification process fails.
     */
    public void removeAlias(String routingAlias) throws Exception {

        Preconditions.checkNotNull(routingAlias);

        this.routingKeys.remove(routingAlias);

        verify();
    }

    @Override
    public void setup() throws Exception {

        verify();
    }

    @Override
    public void cleanup() throws Exception {}

    // TODO: Finish
    @Override
    public void set(Map<String, String> properties) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<String, String> getExtensionProperties() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
