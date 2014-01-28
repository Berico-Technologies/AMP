package amp.topology.protocols.rabbit;

import amp.rabbit.topology.ConsumingRoute;
import amp.rabbit.topology.ProducingRoute;
import amp.topology.global.impl.BasePartition;
import amp.topology.global.filtering.RouteFilterResults;
import amp.topology.protocols.rabbit.requirements.RabbitRouteRequirements;
import amp.topology.protocols.rabbit.topology.ConsumingRouteProvider;
import amp.topology.protocols.rabbit.topology.ProducingRouteProvider;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class RouteFilterResultsAdaptorTest {

    @Test
    public void test_convertProducingRoutes() throws Exception {

        ProducingRoute mockProducingRoute1 = mock(ProducingRoute.class);

        ProducingRouteProvider mockProducingRouteProvider1 = mock(ProducerRouteProviderPartition.class);

        doReturn(mockProducingRoute1)
                .when(mockProducingRouteProvider1)
                .getProducingRoute(any(RabbitRouteRequirements.class));

        ProducingRoute mockProducingRoute2 = mock(ProducingRoute.class);

        ProducingRouteProvider mockProducingRouteProvider2 = mock(ProducerRouteProviderPartition.class);

        doReturn(mockProducingRoute2)
                .when(mockProducingRouteProvider2)
                .getProducingRoute(any(RabbitRouteRequirements.class));

        RouteFilterResults mockFilterResults = mock(RouteFilterResults.class);

        doReturn(Arrays.asList(mockProducingRouteProvider1, mockProducingRouteProvider2))
                .when(mockFilterResults)
                .getProducerPartitions();

        RabbitRouteRequirements mockRequirements = mock(RabbitRouteRequirements.class);

        InOrder inOrder = inOrder(
                mockProducingRoute1,
                mockProducingRoute2,
                mockProducingRouteProvider1,
                mockProducingRouteProvider2,
                mockFilterResults,
                mockRequirements);

        Collection<ProducingRoute> producingRoutes =
                RouteFilterResultsAdaptor.convertProducingRoutes(mockFilterResults, mockRequirements);

        inOrder.verify(mockFilterResults).getProducerPartitions();

        inOrder.verify(mockProducingRouteProvider1).getProducingRoute(mockRequirements);

        inOrder.verify(mockProducingRouteProvider2).getProducingRoute(mockRequirements);

        assertEquals(2, producingRoutes.size());

        assertTrue(producingRoutes.contains(mockProducingRoute1));

        assertTrue(producingRoutes.contains(mockProducingRoute2));
    }

    @Test
    public void test_convertConsumingRoutes() throws Exception {

        ConsumingRoute mockConsumingRoute1 = mock(ConsumingRoute.class);

        ConsumingRouteProvider mockConsumingRouteProvider1 = mock(ConsumerRouteProviderPartition.class);

        doReturn(mockConsumingRoute1)
                .when(mockConsumingRouteProvider1)
                .getConsumingRoute(any(RabbitRouteRequirements.class));

        ConsumingRoute mockConsumingRoute2 = mock(ConsumingRoute.class);

        ConsumingRouteProvider mockConsumingRouteProvider2 = mock(ConsumerRouteProviderPartition.class);

        doReturn(mockConsumingRoute2)
                .when(mockConsumingRouteProvider2)
                .getConsumingRoute(any(RabbitRouteRequirements.class));

        RouteFilterResults mockFilterResults = mock(RouteFilterResults.class);

        doReturn(Arrays.asList(mockConsumingRouteProvider1, mockConsumingRouteProvider2))
                .when(mockFilterResults)
                .getConsumerPartitions();

        RabbitRouteRequirements mockRequirements = mock(RabbitRouteRequirements.class);

        InOrder inOrder = inOrder(
                mockConsumingRoute1,
                mockConsumingRoute2,
                mockConsumingRouteProvider1,
                mockConsumingRouteProvider2,
                mockFilterResults,
                mockRequirements);

        Collection<ConsumingRoute> consumingRoutes =
                RouteFilterResultsAdaptor.convertConsumingRoutes(mockFilterResults, mockRequirements);

        inOrder.verify(mockFilterResults).getConsumerPartitions();

        inOrder.verify(mockConsumingRouteProvider1).getConsumingRoute(mockRequirements);

        inOrder.verify(mockConsumingRouteProvider2).getConsumingRoute(mockRequirements);

        assertEquals(2, consumingRoutes.size());

        assertTrue(consumingRoutes.contains(mockConsumingRoute1));

        assertTrue(consumingRoutes.contains(mockConsumingRoute2));
    }

    // Necessary to test the implementation.
    public static abstract class ProducerRouteProviderPartition extends BasePartition implements ProducingRouteProvider {}

    // Necessary to test the implementation.
    public static abstract class ConsumerRouteProviderPartition extends BasePartition implements ConsumingRouteProvider {}
}
