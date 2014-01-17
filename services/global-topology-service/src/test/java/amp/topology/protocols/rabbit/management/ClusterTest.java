package amp.topology.protocols.rabbit.management;

import amp.rabbit.topology.Broker;
import org.junit.Test;
import rabbitmq.mgmt.RabbitMgmtService;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class ClusterTest {


    @Test
    public void test_getClusterId(){

        Cluster cluster = new Cluster("cluster1", "/");

        assertEquals("cluster1+/", cluster.getClusterId());
    }

    @Test
    public void test_add(){

        Cluster cluster = spy(new Cluster("cluster1", "/"));

        Broker broker = mock(Broker.class);

        doReturn("/").when(broker).getVirtualHost();

        cluster.add(broker);

        verify(cluster).validate(broker);

        assertTrue(cluster.add("localhost", 5672));

        assertFalse( cluster.add("localhost", 5672) );

        assertTrue( cluster.add("localhost", 5673, "ssl") );

        verify(cluster, times(4)).add(any(Broker.class));
    }


    @Test
    public void test_validate(){

        Cluster cluster = new Cluster("cluster1", "/");

        Broker validBroker = mock(Broker.class);

        doReturn("/").when(validBroker).getVirtualHost();

        assertTrue(cluster.validate(validBroker));

        Broker invalidBroker = mock(Broker.class);

        doReturn("/test").when(invalidBroker).getVirtualHost();

        assertFalse(cluster.validate(invalidBroker));
    }

    @Test
    public void test_addManagementEndpoint() throws Exception {

        Cluster cluster = new Cluster("cluster1", "/");

        cluster.managementEndpoints = spy(cluster.managementEndpoints);

        ManagementEndpoint managementEndpoint1 = createMockManagementEndpoint("localhost", 15672, true);

        ManagementEndpoint managementEndpoint2 = createMockManagementEndpoint("localhost", 15673, true);

        cluster.addManagementEndpoint(managementEndpoint1);

        cluster.addManagementEndpoint(managementEndpoint2);

        verify(cluster.managementEndpoints).add(managementEndpoint1);

        verify(cluster.managementEndpoints).add(managementEndpoint2);

        assertEquals(2, cluster.managementEndpoints.size());

        assertTrue(cluster.managementEndpoints.contains(managementEndpoint1));

        assertTrue(cluster.managementEndpoints.contains(managementEndpoint2));
    }

    @Test(expected = ManagementEndpointAlreadyExistsException.class)
    public void an_error_occurs_when_an_attempt_is_made_to_add_existing_ManagementEndpoint() throws Exception {

        Cluster cluster = new Cluster("cluster1", "/");

        ManagementEndpoint managementEndpoint1 = createMockManagementEndpoint("localhost", 15672, true);

        ManagementEndpoint managementEndpoint2 = createMockManagementEndpoint("localhost", 15672, true);

        cluster.addManagementEndpoint(managementEndpoint1);

        cluster.addManagementEndpoint(managementEndpoint2);

        fail("An exception should have occurred in the previous statement.");
    }

    @Test
    public void test_removeManagementEndpoint() throws Exception {

        Cluster cluster = new Cluster("cluster1", "/");

        cluster.managementEndpoints = spy(cluster.managementEndpoints);

        ManagementEndpoint managementEndpoint1 = createMockManagementEndpoint("localhost", 15672, true);

        ManagementEndpoint managementEndpoint2 = createMockManagementEndpoint("localhost", 15673, true);

        ManagementEndpoint managementEndpoint3 = createMockManagementEndpoint("localhost", 15674, true);

        cluster.managementEndpoints.addAll(Arrays.asList(managementEndpoint1, managementEndpoint2, managementEndpoint3));

        cluster.removeManagementEndpoint(managementEndpoint2.getId());

        verify(cluster.managementEndpoints).remove(managementEndpoint2);

        assertEquals(2, cluster.managementEndpoints.size());

        assertTrue(cluster.managementEndpoints.contains(managementEndpoint1));

        assertTrue(cluster.managementEndpoints.contains(managementEndpoint3));
    }

    @Test(expected = ManagementEndpointNotExistException.class)
    public void an_error_occurs_when_an_attempt_is_made_to_remove_a_nonexistent_ManagementEndpoint() throws Exception {

        Cluster cluster = new Cluster("cluster1", "/");

        ManagementEndpoint managementEndpoint1 = createMockManagementEndpoint("localhost", 15672, true);

        ManagementEndpoint managementEndpoint2 = createMockManagementEndpoint("localhost", 15673, true);

        cluster.managementEndpoints.add(managementEndpoint1);

        cluster.removeManagementEndpoint(managementEndpoint2.getId());

        fail("An exception should have occurred in the previous statement.");
    }

    @Test
    public void test_executeManagementTask() throws Exception {

        Cluster cluster = new Cluster("cluster1", "/");

        ManagementEndpoint managementEndpoint1 = createMockManagementEndpoint("localhost", 15672, true);

        cluster.addManagementEndpoint(managementEndpoint1);

        cluster.executeManagementTask(new NoOpRmqManagementTask());

        verify(managementEndpoint1).getManagementService();
    }

    @Test
    public void executeManagementTask_succeeds_if_at_least_one_ManagementEndpoint_succeeds() throws Exception {

        Cluster cluster = new Cluster("cluster1", "/");

        ManagementEndpoint managementEndpoint1 = createMockManagementEndpoint("localhost", 15672, true);

        when(managementEndpoint1.getManagementService()).thenThrow(Exception.class);

        ManagementEndpoint managementEndpoint2 = createMockManagementEndpoint("localhost", 15673, true);

        when(managementEndpoint2.getManagementService()).thenThrow(Exception.class);

        ManagementEndpoint managementEndpoint3 = createMockManagementEndpoint("localhost", 15674, true);

        cluster.managementEndpoints.addAll(
                Arrays.asList(managementEndpoint1, managementEndpoint2, managementEndpoint3));

        cluster.executeManagementTask(new NoOpRmqManagementTask());

        verify(managementEndpoint1).getManagementService();
        verify(managementEndpoint2).getManagementService();
        verify(managementEndpoint3).getManagementService();
    }

    @Test(expected = ManagementTaskFailedOnAllEndpointsException.class)
    public void an_error_occurs_if_all_management_tasks_fail() throws Exception {

        Cluster cluster = new Cluster("cluster1", "/");

        ManagementEndpoint managementEndpoint1 = createMockManagementEndpoint("localhost", 15672, true);

        when(managementEndpoint1.getManagementService()).thenThrow(Exception.class);

        ManagementEndpoint managementEndpoint2 = createMockManagementEndpoint("localhost", 15673, true);

        when(managementEndpoint2.getManagementService()).thenThrow(Exception.class);

        cluster.managementEndpoints.addAll(
                Arrays.asList(managementEndpoint1, managementEndpoint2));

        cluster.executeManagementTask(new NoOpRmqManagementTask());

        fail("An exception should have occurred in the previous statement.");
    }

    @Test
    public void listener_is_called_when_broker_is_added() throws Exception {

        Cluster cluster = new Cluster("cluster1", "/");

        Cluster.Listener listener = mock(Cluster.Listener.class);

        cluster.addListener(listener);

        Broker broker = mock(Broker.class);

        doReturn("/").when(broker).getVirtualHost();

        cluster.add(broker);

        verify(listener).onBrokerAdded(cluster, broker);
    }

    @Test
    public void listener_is_called_when_broker_is_removed() throws Exception {

        Cluster cluster = new Cluster("cluster1", "/");

        Broker broker = mock(Broker.class);

        doReturn("/").when(broker).getVirtualHost();

        cluster.add(broker);

        Cluster.Listener listener = mock(Cluster.Listener.class);

        cluster.addListener(listener);

        cluster.remove(broker);

        verify(listener).onBrokerRemoved(cluster, broker);
    }

    @Test
    public void listener_is_called_when_management_endpoint_is_added() throws Exception {

        Cluster cluster = new Cluster("cluster1", "/");

        Cluster.Listener listener = mock(Cluster.Listener.class);

        cluster.addListener(listener);

        ManagementEndpoint managementEndpoint = createMockManagementEndpoint("localhost", 15672, true);

        cluster.addManagementEndpoint(managementEndpoint);

        verify(listener).onManagementEndpointAdded(cluster, managementEndpoint);
    }

    @Test
    public void listener_is_called_when_management_endpoint_is_removed() throws Exception {

        Cluster cluster = new Cluster("cluster1", "/");

        ManagementEndpoint managementEndpoint = createMockManagementEndpoint("localhost", 15672, true);

        cluster.addManagementEndpoint(managementEndpoint);

        Cluster.Listener listener = mock(Cluster.Listener.class);

        cluster.addListener(listener);

        cluster.removeManagementEndpoint(managementEndpoint.getId());

        verify(listener).onManagementEndpointRemoved(cluster, managementEndpoint);
    }

    static ManagementEndpoint createMockManagementEndpoint(String hostname, int port, boolean useSsl){

        ManagementEndpoint managementEndpoint = mock(ManagementEndpoint.class);

        doReturn(hostname).when(managementEndpoint).getHostname();

        doReturn(port).when(managementEndpoint).getPort();

        doReturn(hostname + ":" + port).when(managementEndpoint).getId();

        RabbitMgmtService rabbitMgmtService = mock(RabbitMgmtService.class);

        doReturn(rabbitMgmtService).when(managementEndpoint).getManagementService();

        return managementEndpoint;
    }

    static class NoOpRmqManagementTask implements Cluster.ManagementTask<Void> {

        @Override
        public Void execute(RabbitMgmtService rmq) throws Exception {

            rmq.exchanges();

            return null;
        }
    }
}
