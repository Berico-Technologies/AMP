package amp.topology.protocols.rabbit.topology;

import amp.rabbit.topology.Exchange;
import amp.topology.protocols.rabbit.management.Cluster;
import amp.topology.protocols.rabbit.management.ManagementEndpoint;
import amp.topology.protocols.rabbit.topology.exceptions.ExchangeDoesNotExistException;
import org.junit.Test;
import rabbitmq.mgmt.RabbitMgmtService;

import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class BaseRabbitPartitionTest extends RabbitTopologyTestBase {

    @Test
    public void test_setup() throws Exception {

        RabbitMgmtService rmq = createMockManagementService();

        ManagementEndpoint me = createMockManagementEndpoint(rmq);

        Cluster cluster = createMockCluster(me);

        Exchange exchange = createMockExchange("amq.direct");

        Collection<String> routingKeys = rkeys("my.test.topic");

        TBaseRabbitPartition partition = new TBaseRabbitPartition(cluster, exchange, routingKeys);

        assertEquals(amp.topology.global.Partition.PartitionStates.NONEXISTENT, partition.getState());

        partition.setup();

        verify(rmq.exchanges()).create(any(rabbitmq.mgmt.model.Exchange.class));

        assertEquals(amp.topology.global.Partition.PartitionStates.ACTIVE, partition.getState());
    }

    @Test
    public void test_verify__exchange_present() throws Exception {

        RabbitMgmtService rmq = createMockManagementService();

        ManagementEndpoint me = createMockManagementEndpoint(rmq);

        Cluster cluster = createMockCluster(me);

        Exchange exchange = createMockExchange("amq.direct");

        Collection<String> routingKeys = rkeys("my.test.topic");

        TBaseRabbitPartition partition = new TBaseRabbitPartition(cluster, exchange, routingKeys);

        partition.setup();

        assertEquals(amp.topology.global.Partition.PartitionStates.ACTIVE, partition.getState());

        rabbitmq.mgmt.model.Exchange rmqExchange = mock(rabbitmq.mgmt.model.Exchange.class);

        when(rmq.exchanges().get("/", "amq.direct")).thenReturn(rmqExchange);

        partition.verify();

        verify(rmq.exchanges()).get("/", "amq.direct");
    }

    @Test(expected = ExchangeDoesNotExistException.class)
     public void test_verify__exchange_absent() throws Exception {

        RabbitMgmtService rmq = createMockManagementService();

        ManagementEndpoint me = createMockManagementEndpoint(rmq);

        Cluster cluster = createMockCluster(me);

        Exchange exchange = createMockExchange("unknown");

        Collection<String> routingKeys = rkeys("my.test.topic");

        TBaseRabbitPartition partition = new TBaseRabbitPartition(cluster, exchange, routingKeys);

        partition.setup();

        assertEquals(amp.topology.global.Partition.PartitionStates.ACTIVE, partition.getState());

        when(rmq.exchanges().get("/", "unknown")).thenReturn(null);

        partition.verify();
    }

    @Test
    public void test_cleanup() throws Exception {

        RabbitMgmtService rmq = createMockManagementService();

        ManagementEndpoint me = createMockManagementEndpoint(rmq);

        Cluster cluster = createMockCluster(me);

        Exchange exchange = createMockExchange("test.exchange");

        Collection<String> routingKeys = rkeys("my.test.topic");

        TBaseRabbitPartition partition = new TBaseRabbitPartition(cluster, exchange, routingKeys);

        partition.setup();

        assertEquals(amp.topology.global.Partition.PartitionStates.ACTIVE, partition.getState());

        partition.cleanup();

        verify(rmq.exchanges()).delete("/", "test.exchange");

        assertEquals(amp.topology.global.Partition.PartitionStates.NONEXISTENT, partition.getState());
    }
}
