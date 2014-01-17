package amp.topology.protocols.rabbit.topology;

import amp.topology.global.Connector;
import amp.topology.protocols.rabbit.management.Cluster;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class RabbitInterVHostConnectorTest extends RabbitTopologyTestBase {

    @Test
    public void test_verify__should_pass_validation() throws Exception {

        RabbitInterVHostConnector connector = createConnector(null, null, rkeys("test.Route"));

        connector.verify();


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
