package amp.topology.protocols.rabbit.topology;

import amp.rabbit.topology.Exchange;
import amp.topology.protocols.rabbit.management.Cluster;
import amp.topology.protocols.rabbit.management.ManagementEndpoint;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import rabbitmq.mgmt.ExchangeOperations;
import rabbitmq.mgmt.RabbitMgmtService;

import java.util.Arrays;
import java.util.Collection;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class RabbitTopologyTestBase {


    static Exchange createMockExchange(String exchangeName){

        Exchange exchange = mock(Exchange.class);

        when(exchange.getName()).thenReturn(exchangeName);

        return exchange;
    }

    static Cluster createMockCluster(final ManagementEndpoint me) throws Exception {

        Cluster cluster = mock(Cluster.class);

        when(cluster.getVirtualHost()).thenReturn("/");

        if (me != null){

            when(cluster.executeManagementTask(any(Cluster.ManagementTask.class))).thenAnswer(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

                    Cluster.ManagementTask<?> task = (Cluster.ManagementTask)invocationOnMock.getArguments()[0];

                    return task.execute(me.getManagementService());
                }
            });
        }

        return cluster;
    }

    static RabbitMgmtService createMockManagementService(){

        RabbitMgmtService rmq = mock(RabbitMgmtService.class);

        ExchangeOperations ops = mock(ExchangeOperations.class);

        when(rmq.exchanges()).thenReturn(ops);

        return rmq;
    }

    static ManagementEndpoint createMockManagementEndpoint(RabbitMgmtService rmq){

        ManagementEndpoint managementEndpoint = mock(ManagementEndpoint.class);

        if (rmq != null)
            when(managementEndpoint.getManagementService()).thenReturn(rmq);

        return managementEndpoint;
    }

    static Collection<String> rkeys(String... keys){ return Arrays.asList(keys); }

    /**
     * An instance for testing purposes.
     */
    static class TBaseRabbitPartition extends BaseRabbitPartition {

        /**
         * Initialize the partition with the Cluster, Exchange, and Routing Key Info.
         *
         * @param cluster     the Cluster in which the Exchange should exist.
         * @param exchange    Exchange configuration.
         * @param routingKeys Routing Keys for producing or consuming on the exchange.
         */
        public TBaseRabbitPartition(Cluster cluster, Exchange exchange, Collection<String> routingKeys) {
            super(cluster, exchange, routingKeys);
        }
    }

}
