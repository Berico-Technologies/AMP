package amp.topology.core;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import amp.rabbit.topology.RoutingInfo;
import amp.rabbit.topology.ITopologyService;

public class GlobalTopologyService implements ITopologyService {
	
	private static final Logger logger = LoggerFactory.getLogger(GlobalTopologyService.class);
	
	public static final String HEADER_REQUEST_TOPO_CREATION = "amp.topology.request.createForClient";
	public static final String HEADER_PREFERRED_QUEUENAME = "amp.topology.request.prefs.queue.name";
	public static final String HEADER_PREFERRED_QUEUE_PREFIX = "amp.topology.request.prefs.queue.prefix";
	
	//ITopologyRepository topologyRepository;
	
	RouteCreator routeCreator = null;

	
	@Override
	public RoutingInfo getRoutingInfo(Map<String, String> routingHints) {
		/*
		String topic = routingHints.get(EnvelopeHeaderConstants.MESSAGE_TOPIC);
		String client = routingHints.get(EnvelopeHeaderConstants.MESSAGE_SENDER_IDENTITY);
		
		ArrayList<RouteInfo> routeInfos = new ArrayList<RouteInfo>();
		
		Collection<ExtendedRouteInfo> extendedRouteInfos = this.topologyRepository.find(topic, client);
		
		for (ExtendedRouteInfo extendedRouteInfo : extendedRouteInfos){
			
			Exchange producerExchange = 
				this.topologyRepository.getExchange(
					extendedRouteInfo.getProducerExchangeId())
						.toExchange();
			
			ExtendedExchange consumerExtendedExchange =
				this.topologyRepository.getExchange(
					extendedRouteInfo.getConsumerExchangeId());
			
			Exchange consumerExchange = consumerExtendedExchange.toExchange();
			
			routeInfos.add(new RouteInfo(producerExchange, consumerExchange));
		}
		
		if (shouldPrecreateRoute(routingHints) && routeCreator != null){
			
			logger.info("Route precreation requested, delegating to the RouteCreator.");
			
			TopologyRequestContext context = 
				new TopologyRequestContext(
						client, topic, 
						routingHints.get(HEADER_PREFERRED_QUEUE_PREFIX), 
						routingHints.get(HEADER_PREFERRED_QUEUENAME));
			
			routeCreator.create(routeInfos, context);
		}
		
		return new RoutingInfo(routeInfos);*/
		return null;
	}

//	protected boolean shouldPrecreateRoute(Map<String, String> routingHints){
//		
//		if (routingHints.containsKey(HEADER_REQUEST_TOPO_CREATION)){
//			
//			return !routingHints.get(HEADER_REQUEST_TOPO_CREATION).equals("false");
//		}
//		return false;
//	}
	
	
	@Override
	public void dispose() {}
	
}
