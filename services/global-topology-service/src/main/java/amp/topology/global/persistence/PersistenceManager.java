package amp.topology.global.persistence;

/**
 * A Facade for Managing Persistence of all of the different topology items.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class PersistenceManager {

    private static TopologyStatePersister.TopicStatePersister topicStatePersister;

    private static TopologyStatePersister.GroupStatePersister groupStatePersister;

    private static TopologyStatePersister.ConnectorStatePersister connectorStatePersister;

    private static TopologyStatePersister.PartitionStatePersister partitionStatePersister;

    public static void setTopicStatePersister(TopologyStatePersister.TopicStatePersister topicStatePersister) {

        PersistenceManager.topicStatePersister = topicStatePersister;
    }

    public static void setGroupStatePersister(TopologyStatePersister.GroupStatePersister groupStatePersister) {

        PersistenceManager.groupStatePersister = groupStatePersister;
    }

    public static void setConnectorStatePersister(TopologyStatePersister.ConnectorStatePersister connectorStatePersister) {

        PersistenceManager.connectorStatePersister = connectorStatePersister;
    }

    public static void setPartitionStatePersister(TopologyStatePersister.PartitionStatePersister partitionStatePersister) {

        PersistenceManager.partitionStatePersister = partitionStatePersister;
    }

    public static TopologyStatePersister.TopicStatePersister topics(){ return topicStatePersister; }

    public static TopologyStatePersister.GroupStatePersister groups(){ return groupStatePersister; }

    public static TopologyStatePersister.ConnectorStatePersister connectors(){ return  connectorStatePersister; }

    public static TopologyStatePersister.PartitionStatePersister partitions(){ return partitionStatePersister; }
}
