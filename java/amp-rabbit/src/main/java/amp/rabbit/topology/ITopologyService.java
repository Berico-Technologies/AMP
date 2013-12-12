package amp.rabbit.topology;


import java.util.Map;

import cmf.bus.IDisposable;


public interface ITopologyService extends IDisposable {

    RoutingInfo getRoutingInfo(Map<String, String> routingHints);
}
