package amp.topology.global.lifecycle;

import amp.topology.global.lifecycle.TopologyStatePersister.ConnectorStatePersister;
import amp.topology.global.lifecycle.TopologyStatePersister.GroupStatePersister;
import amp.topology.global.lifecycle.TopologyStatePersister.PartitionStatePersister;
import amp.topology.global.lifecycle.TopologyStatePersister.TopicStatePersister;

/**
 * A Facade for Managing Persistence of all of the different topology items.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class PersistenceManager {

    private static TopicStatePersister topicStatePersister;

    private static GroupStatePersister groupStatePersister;

    private static ConnectorStatePersister connectorStatePersister;

    private static PartitionStatePersister partitionStatePersister;

    public static void setTopicStatePersister(TopicStatePersister topicStatePersister) {

        PersistenceManager.topicStatePersister = topicStatePersister;
    }

    public static void setGroupStatePersister(GroupStatePersister groupStatePersister) {

        PersistenceManager.groupStatePersister = groupStatePersister;
    }

    public static void setConnectorStatePersister(ConnectorStatePersister connectorStatePersister) {

        PersistenceManager.connectorStatePersister = connectorStatePersister;
    }

    public static void setPartitionStatePersister(PartitionStatePersister partitionStatePersister) {

        PersistenceManager.partitionStatePersister = partitionStatePersister;
    }

    public static TopicStatePersister topics(){ return topicStatePersister; }

    public static GroupStatePersister groups(){ return groupStatePersister; }

    public static ConnectorStatePersister connectors(){ return  connectorStatePersister; }

    public static PartitionStatePersister partitions(){ return partitionStatePersister; }
}
