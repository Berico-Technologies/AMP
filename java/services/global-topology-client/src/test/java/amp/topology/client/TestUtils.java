package amp.topology.client;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import amp.rabbit.topology.BaseRoute;
import amp.rabbit.topology.Broker;
import amp.rabbit.topology.ConsumingRoute;
import amp.rabbit.topology.Exchange;
import amp.rabbit.topology.Queue;
import amp.rabbit.topology.RoutingInfo;

import amp.messaging.EnvelopeHeaderConstants;

public class TestUtils {

	private static final Logger logger = LoggerFactory.getLogger(TestUtils.class);
	
	public static Map<String, String> buildRoutingHints(String topic){
		
		Map<String, String> routingHints = new HashMap<String, String>();
		
		routingHints.put(EnvelopeHeaderConstants.MESSAGE_TOPIC, topic);
		
		return routingHints;
	}
	
	public static void dumpRoutingInfoToLogger(RoutingInfo routingInfo){
		
		logger.debug("Routing Info Dump: ");
		
		logger.debug("PRODUCER ROUTE(s): ");
		for(BaseRoute route : routingInfo.getProducingRoutes()){			
			dumpRouteInfoToLogger(route);
		}
		logger.debug("CONSUME ROUTE(s): ");
		for(BaseRoute route : routingInfo.getConsumingRoutes()){			
			dumpRouteInfoToLogger(route);
		}
	}
	
	public static void dumpRouteInfoToLogger(BaseRoute route){
		logger.debug("BeginRouteDef:");
		logger.debug("  >>  BROKER(s): ");
		for (Broker broker: route.getBrokers()) {
			logger.debug("\t\tBroker: "+broker);
		}
		
		Exchange exchange = route.getExchange();
		logger.debug("  >>  EXCHANGE INFO: ");
		logger.debug("\t\tExchange Name: {}", exchange.getName());
		logger.debug("\t\tExchange Type: {}", exchange.getExchangeType());		
		logger.debug("\t\tIs Durable: {}", exchange.isDurable());
		logger.debug("\t\tIs Auto Delete: {}", exchange.isAutoDelete());
		
		if (route instanceof ConsumingRoute) {
			
			
			Queue queue = ((ConsumingRoute)route).getQueue();
			logger.debug("  >>  Queue INFO: ");
			logger.debug("\t\tQueue Name: {}", queue.getName());				
			logger.debug("\t\tIs Exclusive: {}", queue.isExclusive());
			logger.debug("\t\tIs Durable: {}", queue.isDurable());
			logger.debug("\t\tIs Auto Delete: {}", queue.isAutoDelete());
		}
		
		
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (String key : route.getRoutingKeys()) {
			if (sb.length()>2)
				sb.append(",");
			sb.append(key);
		}
		sb.append("]");
		logger.debug("  >>  ROUTE KEYS: "+sb.toString());
		logger.debug("EndRoute");
	}
}
