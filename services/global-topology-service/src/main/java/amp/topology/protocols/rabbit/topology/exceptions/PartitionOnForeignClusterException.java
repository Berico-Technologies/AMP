package amp.topology.protocols.rabbit.topology.exceptions;

import amp.topology.protocols.rabbit.management.Cluster;
import amp.topology.protocols.rabbit.topology.BaseRabbitPartition;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class PartitionOnForeignClusterException extends Exception {

    private final Cluster cluster;

    private final BaseRabbitPartition partition;

    public PartitionOnForeignClusterException(Cluster cluster, BaseRabbitPartition partition) {

        super(String.format("BasePartition '%s' is on cluster '%s' and should be on cluster '%s'.",
                partition.getPartitionId(), partition.getCluster().getClusterId(), cluster.getClusterId()));

        this.cluster = cluster;
        this.partition = partition;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public BaseRabbitPartition getPartition() {
        return partition;
    }
}
