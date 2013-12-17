package amp.topology.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import amp.rabbit.topology.Exchange;
//import amp.rabbit.topology.RouteInfo;
import org.junit.Test;



public class RmqRouteCreatorTest {

//	@Test
//	public void buildQualifiedName_returns_the_correct_string_for_exchanges() {
//
//		//RabbitMgmtService mgmt = mock(RabbitMgmtService.class);
//
//		RmqRouteCreator creator = new RmqRouteCreator(null);
//
//		Exchange ex = mock(Exchange.class);
//
//		when(ex.getHostName()).thenReturn("localhost");
//		when(ex.getPort()).thenReturn(5672);
//		when(ex.getVirtualHost()).thenReturn("/");
//
//		String actualContextKey = creator.buildQualifiedName(ex, "test.exchange");
//
//		assertEquals("amqp://localhost:5672/test.exchange", actualContextKey);
//	}
//
//	@Test
//	public void addIfNotExists_adds_entry_when_there_is_no_existing_key(){
//
//		RmqRouteCreator creator = new RmqRouteCreator(null);
//
//		Exchange ex = mock(Exchange.class);
//
//		HashMap<String, Exchange> map = new HashMap<String, Exchange>();
//
//		creator.addIfNotExists(map, "test", ex);
//
//		assertTrue(map.containsKey("test"));
//	}
//
//	@Test
//	public void addIfNotExists_ignores_entry_when_there_is_an_existing_key(){
//
//		RmqRouteCreator creator = new RmqRouteCreator(null);
//
//		Exchange ex = mock(Exchange.class);
//
//		HashMap<String, Exchange> map = spy(new HashMap<String, Exchange>());
//
//		map.put("test", ex);
//
//		creator.addIfNotExists(map, "test", ex);
//
//		verify(map, times(1)).put("test", ex);
//	}
//
//	@Test
//	public void getUniqueExchanges_returns_only_unique_exchanges(){
//
//		RmqRouteCreator creator = new RmqRouteCreator(null);
//
//		Exchange ex = mock(Exchange.class);
//
//		when(ex.getName()).thenReturn("test.exchange");
//		when(ex.getHostName()).thenReturn("localhost");
//		when(ex.getPort()).thenReturn(5672);
//		when(ex.getVirtualHost()).thenReturn("/");
//
//		Collection<RouteInfo> routes = Arrays.asList(new RouteInfo(ex, ex));
//
//		Collection<Exchange> uniqExchanges = creator.getUniqueExchanges(routes);
//
//		assertEquals(1, uniqExchanges.size());
//
//		Exchange actual = uniqExchanges.iterator().next();
//
//		assertEquals(ex, actual);
//	}
	
}
