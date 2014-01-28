package amp.topology.protocols.rabbit;

import amp.rabbit.topology.RoutingInfo;
import amp.topology.global.impl.BasePartition;
import amp.topology.global.impl.BaseTopic;
import amp.topology.global.TopicRegistry;
import amp.topology.global.filtering.RouteFilterResults;
import amp.topology.global.filtering.RouteRequirements;
import amp.topology.protocols.rabbit.requirements.RabbitRouteRequirements;
import org.junit.Test;
import org.mockito.InOrder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the RabbitRouteProvider REST Endpoint.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class RabbitRouteProviderResourceTest {

    @Test
    public void test_getRoutingInfo_v3_3_0() throws Exception {

        UserDetails mockUserDetails = mock(UserDetails.class);

        RabbitRouteRequirements mockRouteRequirements = mock(RabbitRouteRequirements.class);

        doReturn("abc123").when(mockRouteRequirements).getTopic();

        TopicRegistry mockTopicRegistry = mock(TopicRegistry.class);

        RouteFilterResults mockResults = createMockRouteFilterResults();

        amp.topology.global.Topic mockTopic = createMockTopic(mockResults);

        doReturn(mockTopic).when(mockTopicRegistry).get(anyString());

        InOrder inOrder = inOrder(
                mockUserDetails,
                mockRouteRequirements,
                mockTopicRegistry,
                mockTopic,
                mockResults);

        RabbitRouteProviderResource resource = new RabbitRouteProviderResource();

        resource.setTopicRegistry(mockTopicRegistry);

        RoutingInfo routingInfo = resource.getRoutingInfo(mockUserDetails, mockRouteRequirements);

        inOrder.verify(mockTopicRegistry).get("abc123");

        inOrder.verify(mockTopic).filter(mockRouteRequirements);

        inOrder.verify(mockResults).getProducerPartitions();

        inOrder.verify(mockResults).getConsumerPartitions();

        assertNotNull(routingInfo);
    }

    public RouteFilterResults createMockRouteFilterResults(){

        RouteFilterResults mockResults = mock(RouteFilterResults.class);

        doReturn(new ArrayList<BasePartition>()).when(mockResults).getProducerPartitions();

        doReturn(new ArrayList<BasePartition>()).when(mockResults).getConsumerPartitions();

        return mockResults;
    }

    public amp.topology.global.Topic createMockTopic(RouteFilterResults mockResults){

        amp.topology.global.Topic mockTopic = mock(BaseTopic.class);

        doReturn(mockResults).when(mockTopic).filter(any(RouteRequirements.class));

        return mockTopic;
    }

}
