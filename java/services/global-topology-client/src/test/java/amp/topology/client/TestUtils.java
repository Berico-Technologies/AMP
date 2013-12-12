package amp.topology.client;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import amp.rabbit.topology.Exchange;
import amp.rabbit.topology.RouteInfo;
import amp.rabbit.topology.RoutingInfo;

import cmf.bus.EnvelopeHeaderConstants;

public class TestUtils {

	private static final Logger logger = LoggerFactory.getLogger(TestUtils.class);
	
	public static Map<String, String> buildRoutingHints(String topic){
		
		Map<String, String> routingHints = new HashMap<String, String>();
		
		routingHints.put(EnvelopeHeaderConstants.MESSAGE_TOPIC, topic);
		
		return routingHints;
	}
	
	public static void dumpRoutingInfoToLogger(RoutingInfo routingInfo){
		
		logger.debug("Routing Info Dump: ");
		
		for(RouteInfo route : routingInfo.getRoutes()){
			
			dumpRouteInfoToLogger(route);
		}
	}
	
	public static void dumpRouteInfoToLogger(RouteInfo route){
		
		logger.debug("PRODUCER EXCHANGE: ");
		
		dumpExchangeToLogger(route.getProducerExchange());
		
		logger.debug("CONSUMER EXCHANGE: ");
		
		dumpExchangeToLogger(route.getConsumerExchange());
	}
	
	public static void dumpExchangeToLogger(Exchange exchange){
		
		logger.debug("\tExchange Name: {}", exchange.getName());
		logger.debug("\tExchange Type: {}", exchange.getExchangeType());
		logger.debug("\tRouting Key: {}", exchange.getRoutingKey());
		logger.debug("\tHostname: {}", exchange.getHostName());
		logger.debug("\tPort: {}", exchange.getPort());
		logger.debug("\tVirtual Host: {}", exchange.getVirtualHost());
		logger.debug("\tIs Durable: {}", exchange.getIsDurable());
		logger.debug("\tIs Auto Delete: {}", exchange.getIsAutoDelete());
	}
	
}
