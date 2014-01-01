package amp.topology.protocols.rabbit.management;

import org.junit.Test;
import rabbitmq.httpclient.HttpClientProvider;
import rabbitmq.mgmt.RabbitMgmtService;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class ManagementEndpointTest {

    @Test
    public void test_getId(){

        HttpClientProvider httpClientProvider = mock(HttpClientProvider.class);

        ManagementEndpoint managementEndpoint = new ManagementEndpoint("localhost", 15672, true, httpClientProvider);

        assertEquals("localhost:15672", managementEndpoint.getId());
    }

    @Test
    public void test_getManagementService(){

        HttpClientProvider httpClientProvider = mock(HttpClientProvider.class);

        ManagementEndpoint managementEndpoint = new ManagementEndpoint("localhost", 15672, true, httpClientProvider);

        RabbitMgmtService rabbitMgmtService = managementEndpoint.getManagementService();

        verify(httpClientProvider).getClient();
    }
}