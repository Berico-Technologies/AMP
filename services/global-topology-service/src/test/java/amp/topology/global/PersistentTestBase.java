package amp.topology.global;

import amp.topology.global.impl.BaseConnector;
import amp.topology.global.impl.BaseConsumerGroup;
import amp.topology.global.impl.BaseProducerGroup;
import amp.topology.global.impl.TestPartition;
import amp.topology.global.persistence.PersistenceManager;
import amp.topology.global.persistence.TopologyStatePersister;
import org.junit.BeforeClass;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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


    public static BaseConnector createMockConnector(String id){

        BaseConnector connector = mock(BaseConnector.class);

        when(connector.getConnectorId()).thenReturn(id);

        return connector;
    }

    public static BaseProducerGroup createMockProducerGroup(String id){

        BaseProducerGroup group = mock(BaseProducerGroup.class);

        when(group.getGroupId()).thenReturn(id);

        return group;
    }

    public static BaseConsumerGroup createMockConsumerGroup(String id){

        BaseConsumerGroup group = mock(BaseConsumerGroup.class);

        when(group.getGroupId()).thenReturn(id);

        return group;
    }

    public static TestPartition createMockPartition(String id){

        TestPartition partition = mock(TestPartition.class);

        when(partition.getPartitionId()).thenReturn(id);

        return partition;
    }
}
