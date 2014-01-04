package amp.topology;

import amp.topology.snapshot.SnapshotHealthCheck;
import amp.topology.snapshot.SnapshotResource;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.config.FilterBuilder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.filter.DelegatingFilterProxy;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
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

        verify(mockEnvironment).addResource(any(SnapshotResource.class));
        verify(mockEnvironment).addHealthCheck(any(SnapshotHealthCheck.class));
    }

}
