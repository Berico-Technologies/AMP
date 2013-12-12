package amp.topology.client;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import amp.rabbit.topology.Broker;
import amp.rabbit.topology.ConsumingRoute;
import amp.rabbit.topology.Exchange;
import amp.rabbit.topology.ProducingRoute;
import amp.rabbit.topology.Queue;
import amp.rabbit.topology.RoutingInfo;
import amp.utility.serialization.GsonSerializer;

public class RoutingInfoWithGsonSerializerTest {
	
	private String SERIALIZED_ROUTING_INFO = "{\"producingRoutes\":[{\"brokers\":[{\"clusterId\":\"clusterIdOne\",\"hostname\":\"devexample.com\",\"port\":5672,\"virtualHost\":\"/\",\"connectionStrategy\":\"default\"}],\"exchange\":{\"exchangeType\":\"topic\",\"name\":\"exchange.cmf.security\",\"isAutoDelete\":true,\"isDurable\":false,\"shouldDeclare\":true},\"routingKeys\":[\"producingTopicAlpha\",\"producingTopicBravo\"]}],\"consumingRoutes\":[{\"queue\":{\"isExclusive\":true,\"name\":\"queue.cmf.security\",\"isAutoDelete\":true,\"isDurable\":false,\"shouldDeclare\":true},\"brokers\":[{\"clusterId\":\"clusterIdOne\",\"hostname\":\"devexample.com\",\"port\":5672,\"virtualHost\":\"/\",\"connectionStrategy\":\"default\"}],\"exchange\":{\"exchangeType\":\"topic\",\"name\":\"exchange.cmf.security\",\"isAutoDelete\":true,\"isDurable\":false,\"shouldDeclare\":true},\"routingKeys\":[\"queueTopic1\"]}]}";
	
	// Create the components of the Route


	private RoutingInfo createTestRoutingInfoObject() {
		
		Broker broker = new Broker("clusterIdOne","devexample.com",5672,"default");
		Exchange exchange = new Exchange("exchange.cmf.security","topic",true,false,true,null);
		Queue queue = new Queue("queue.cmf.security",true,false,true,true,null);
		String[] routeKeys = {"producingTopicAlpha","producingTopicBravo"};
		String[] routeKeys2 = {"queueTopic1"};
		
		List<ProducingRoute> producingRoutes = new ArrayList<ProducingRoute>();
		List<ConsumingRoute> consumingRoutes = new ArrayList<ConsumingRoute>();
		
			
		// Assemble components into main route lists
		ProducingRoute proute= new ProducingRoute(Arrays.asList(broker),exchange,Arrays.asList(routeKeys));
		ConsumingRoute croute = new ConsumingRoute(Arrays.asList(broker), exchange, queue, Arrays.asList(routeKeys2));
		producingRoutes.add(proute);		
		consumingRoutes.add(croute);
		
		//Create Routing Info
		RoutingInfo routingInfo = new RoutingInfo(producingRoutes,consumingRoutes);
		return routingInfo;
	}
	
	@Test
	public void serializes_routing_info_properly() {
		
		RoutingInfo routingInfo  = createTestRoutingInfoObject();
		//Serialize the route
		GsonSerializer serializer = new GsonSerializer();
		String result = serializer.stringSerialize(routingInfo);
		
		
		assertEquals(SERIALIZED_ROUTING_INFO,result);		
	}
	
	@Test
	public void string_deserializes_into_a_proper_routing_info_object() {
		
		GsonSerializer serializer = new GsonSerializer();
		
		RoutingInfo routingInfo = serializer.stringDeserialize(SERIALIZED_ROUTING_INFO, RoutingInfo.class);
		
		RoutingInfo routingInfoExpected  = createTestRoutingInfoObject();
		
		assertEquals(routingInfoExpected,routingInfo);
		
		TestUtils.dumpRoutingInfoToLogger(routingInfo);
	}

}
