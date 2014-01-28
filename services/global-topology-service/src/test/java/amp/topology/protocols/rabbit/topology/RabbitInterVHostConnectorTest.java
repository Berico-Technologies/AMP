package amp.topology.protocols.rabbit.topology;

import amp.topology.global.Connector;
import amp.topology.protocols.rabbit.management.Cluster;
import amp.topology.protocols.rabbit.topology.exceptions.GroupHasNoPartitionsException;
import amp.topology.protocols.rabbit.topology.exceptions.PartitionOnForeignClusterException;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class RabbitInterVHostConnectorTest extends RabbitTopologyTestBase {

    @Test
    public void test_checkHasPartitions__throws_exception_when_there_are_no_partitions() throws Exception {

        RabbitInterVHostConnector connector = createConnector(null, null, rkeys("test.Route"));

        try {

            connector.checkHasPartitions(new ArrayList<BaseRabbitPartition>());

            fail();

        } catch (GroupHasNoPartitionsException e){

            assertEquals(Connector.ConnectorStates.IN_ERROR, connector.getState());
        }
    }

    @Test
    public void test_checkOnSameCluster__throws_exception_when_partition_and_connector_cluster_different() throws Exception {

        RabbitInterVHostConnector connector = createConnector(null, null, rkeys("test.Route"));

        BaseRabbitPartition partition = mock(BaseRabbitPartition.class);

        Cluster DIFFERENT_CLUSTER = mock(Cluster.class);

        when(partition.getCluster()).thenReturn(DIFFERENT_CLUSTER);

        try {

            connector.checkOnSameCluster(connector.cluster, partition);

            fail();

        } catch (PartitionOnForeignClusterException e){

            assertEquals(Connector.ConnectorStates.IN_ERROR, connector.getState());

        }
    }

    @Test
    public void test_checkHasRoutingKeys() throws Exception {

        RabbitInterVHostConnector connector = createConnector(null, null, rkeys("test.Route"));

        final Set<String> PARTITION_KEYS = Sets.newHashSet("key1", "key2");

        BaseRabbitPartition partition = mock(BaseRabbitPartition.class);

        when(partition.getRoutingKeys()).thenReturn(PARTITION_KEYS);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {

                PARTITION_KEYS.add((invocationOnMock.getArguments()[0].toString()));

                return null;
            }
        }).when(partition).addRoutingKey(anyString());

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {

                PARTITION_KEYS.remove((invocationOnMock.getArguments()[0].toString()));

                return null;
            }
        }).when(partition).removeRoutingKey(anyString());

        connector.checkHasRoutingKeys(partition, rkeys("key1", "key3"));

        assertEquals(2, PARTITION_KEYS.size());

        assertTrue(PARTITION_KEYS.contains("key1"));

        assertTrue(PARTITION_KEYS.contains("key3"));
    }


    static RabbitInterVHostConnector createConnector(
            Collection<RabbitProducerPartition> pparts,
            Collection<RabbitConsumerPartition> cparts,
            Collection<String> keys) throws Exception {

        Cluster cluster = createMockCluster(null);

        RabbitProducerGroup pgroup = mock(RabbitProducerGroup.class);

        Collection<RabbitProducerPartition> p = (pparts != null)? pparts : Arrays.asList(
                createMockPPartition("ppart1", cluster),
                createMockPPartition("ppart2", cluster)
        );

        when(pgroup.getPartitions()).thenReturn(p);

        RabbitConsumerGroup cgroup = mock(RabbitConsumerGroup.class);

        Collection<RabbitConsumerPartition> c = (cparts != null)? cparts : Arrays.asList(
                createMockCPartition("cpart1", cluster),
                createMockCPartition("cpart2", cluster)
        );

        when(cgroup.getPartitions()).thenReturn(c);

        return new RabbitInterVHostConnector("description", pgroup, cgroup, cluster, keys);
    }

    static RabbitProducerPartition createMockPPartition(String id, Cluster cluster){

        RabbitProducerPartition partition = mock(RabbitProducerPartition.class);

        when(partition.getId()).thenReturn(id);

        ArrayList<String> keys = Lists.newArrayList();

        when(partition.getRoutingKeys()).thenReturn(keys);

        when(partition.getCluster()).thenReturn(cluster);

        return partition;
    }

    static RabbitConsumerPartition createMockCPartition(String id, Cluster cluster){

        RabbitConsumerPartition partition = mock(RabbitConsumerPartition.class);

        when(partition.getId()).thenReturn(id);

        ArrayList<String> keys = Lists.newArrayList();

        when(partition.getRoutingKeys()).thenReturn(keys);

        when(partition.getCluster()).thenReturn(cluster);

        return partition;
    }

}
