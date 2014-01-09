package amp.topology;

import amp.topology.protocols.rabbit.RabbitRouteProviderResource;
import amp.topology.snapshot.SnapshotHealthCheck;
import amp.topology.snapshot.SnapshotResource;
import amp.topology.support.AccessDeniedMapper;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.config.FilterBuilder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.filter.DelegatingFilterProxy;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Verify that Resources are being loaded.  This is especially helpful when using Fallwizard+Spring to
 * manage the injection of the components into the environment.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class TopologyServiceWireupTest {

    private final FilterBuilder mockFilterBuilder = mock(FilterBuilder.class);
    private final Environment mockEnvironment = mock(Environment.class);
    private final TopologyService topologyService = new TopologyService();
    private final TopologyConfiguration topologyConfiguration = new TopologyConfiguration();

    @Before
    public void setup() throws Exception {

        when(mockEnvironment.addFilter(eq(DelegatingFilterProxy.class), anyString())).thenReturn(mockFilterBuilder);

        topologyConfiguration.getSpringConfiguration().setShouldUseSpringSecurity(true);

        topologyConfiguration.getSpringConfiguration().setApplicationContext(new String[]{
                "configuration/applicationContext.xml"
        });
    }

    @Test
    public void ensure_environment_is_correctly_wired_up() throws Exception {

        topologyService.run(topologyConfiguration, mockEnvironment);

        verify(mockEnvironment).addProvider(isA(AccessDeniedMapper.class));

        verify(mockEnvironment).addResource(isA(SnapshotResource.class));

        verify(mockEnvironment).addResource(isA(RabbitRouteProviderResource.class));

        verify(mockEnvironment).addHealthCheck(any(SnapshotHealthCheck.class));
    }

}
