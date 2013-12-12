package amp.topology.client;

import amp.rabbit.topology.RoutingInfo;

public interface FallbackRoutingInfoProvider {

	public RoutingInfo getFallbackRoute(String topic);
}
