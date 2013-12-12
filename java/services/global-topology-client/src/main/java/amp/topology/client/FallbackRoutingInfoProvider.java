package amp.topology.client;

import java.util.Map;

import amp.rabbit.topology.RoutingInfo;

public interface FallbackRoutingInfoProvider {

	public RoutingInfo getFallbackRoute(Map<String,String> routingHints);
}
