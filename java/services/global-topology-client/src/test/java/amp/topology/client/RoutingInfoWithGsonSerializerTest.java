package amp.topology.client;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;

import amp.rabbit.topology.Exchange;
import amp.rabbit.topology.RouteInfo;
import amp.rabbit.topology.RoutingInfo;
import amp.utility.serialization.GsonSerializer;

public class RoutingInfoWithGsonSerializerTest {
	
	public void assertExchangeHasValues(
		Exchange actual, String name, String type, 
		String routingKey, String host, int port, 
		String vhost, boolean isDurable, boolean isAutoDelete){
		
		assertEquals(actual.getName(), name);
		assertEquals(actual.getExchangeType(), type);
		assertEquals(actual.getRoutingKey(), routingKey);
		assertEquals(actual.getHostName(), host);
		assertEquals(actual.getPort(), port);
		assertEquals(actual.getVirtualHost(), vhost);
		assertEquals(actual.getIsDurable(), isDurable);
		assertEquals(actual.getIsAutoDelete(), isAutoDelete);
	}
	
	@Test
	public void string_deserializes_into_a_proper_routing_info_object() {
		
		String serializedString = "{\"routes\":[{\"consumerExchange\":{\"arguments\":null,\"exchangeType\":\"topic\",\"hostName\":\"devexample.com\",\"isAutoDelete\":false,\"isDurable\":false,\"name\":\"cmf.security\",\"port\":5672,\"queueName\":\"\",\"routingKey\":\"cmf.security\",\"virtualHost\":\"/\"},\"producerExchange\":{\"arguments\":null,\"exchangeType\":\"topic\",\"hostName\":\"devexample.com\",\"isAutoDelete\":false,\"isDurable\":false,\"name\":\"cmf.security\",\"port\":5672,\"queueName\":\"\",\"routingKey\":\"cmf.security\",\"virtualHost\":\"/\"}}]}";
		
		GsonSerializer serializer = new GsonSerializer();
		
		RoutingInfo routingInfo = serializer.stringDeserialize(serializedString, RoutingInfo.class);
		
		Iterator<RouteInfo> routesIterator = routingInfo.getRoutes().iterator();
		
		assertTrue(routesIterator.hasNext());
		
		RouteInfo actualRoute = routesIterator.next();
		
		assertExchangeHasValues(
			actualRoute.getProducerExchange(), 
			"cmf.security", "topic", "cmf.security", "devexample.com", 5672, "/", false, false);
		
		assertExchangeHasValues(
				actualRoute.getConsumerExchange(), 
				"cmf.security", "topic", "cmf.security", "devexample.com", 5672, "/", false, false);
		
		assertFalse(routesIterator.hasNext());
		
		TestUtils.dumpRoutingInfoToLogger(routingInfo);
	}

}
