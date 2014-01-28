package amp.topology.global.impl;

import amp.topology.global.persistence.PersistenceManager;
import amp.topology.global.persistence.TopologyStatePersister;
import org.junit.BeforeClass;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.mock;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class PersistentTestBase {

    @BeforeClass
    public static void registerMocks(){

        if (PersistenceManager.partitions() == null)
            PersistenceManager.setPartitionStatePersister(mock(TopologyStatePersister.PartitionStatePersister.class));

        if (PersistenceManager.connectors() == null)
            PersistenceManager.setConnectorStatePersister(mock(TopologyStatePersister.ConnectorStatePersister.class));

        if (PersistenceManager.groups() == null)
            PersistenceManager.setGroupStatePersister(mock(TopologyStatePersister.GroupStatePersister.class));

        if (PersistenceManager.topics() == null)
            PersistenceManager.setTopicStatePersister(mock(TopologyStatePersister.TopicStatePersister.class));
    }

    public static BasePartition.DehydratedState locatePartitionState(ArgumentCaptor<BasePartition.DehydratedState> captor, String partitionId){

        for (BasePartition.DehydratedState state : captor.getAllValues())
            if (state.getPartitionId().equals(partitionId)) return state;

        return null;
    }

    public static BaseConnector.DehydratedState locateConnectorState(ArgumentCaptor<BaseConnector.DehydratedState> captor, String connectorId){

        for (BaseConnector.DehydratedState state : captor.getAllValues())
            if (state.getConnectorId().equals(connectorId)) return state;

        return null;
    }

    public static BaseGroup.DehydratedState locateGroupState(ArgumentCaptor<BaseGroup.DehydratedState> captor, String groupId){

        for (BaseGroup.DehydratedState state : captor.getAllValues())
            if (state.getGroupId().equals(groupId)) return state;

        return null;
    }

    public static BaseTopic.DehydratedState locateTopicState(ArgumentCaptor<BaseTopic.DehydratedState> captor, String topicId){

        for (BaseTopic.DehydratedState state : captor.getAllValues())
            if (state.getTopicId().equals(topicId)) return state;

        return null;
    }
}
