package amp.topology.core.repo.snapshot;

import java.util.ArrayList;
import java.util.Collection;

import amp.topology.core.ExtendedExchange;
import amp.topology.core.ExtendedRouteInfo;


public class TopologySnapshot {

	protected long timestamp = System.currentTimeMillis();
	
	protected ArrayList<ExtendedExchange> exchanges = new ArrayList<ExtendedExchange>();
	
	protected ArrayList<ExtendedRouteInfo> routes = new ArrayList<ExtendedRouteInfo>();
	
	public TopologySnapshot(){}
	
	public TopologySnapshot(
			Collection<ExtendedExchange> exchanges,
			Collection<ExtendedRouteInfo> routes) {
		
		this.exchanges.addAll(exchanges);
		this.routes.addAll(routes);
	}
	
	public TopologySnapshot(
			long timestamp,
			Collection<ExtendedExchange> exchanges,
			Collection<ExtendedRouteInfo> routes) {
		
		this.timestamp = timestamp;
		this.exchanges.addAll(exchanges);
		this.routes.addAll(routes);
	}

	public long getTimestamp() {
		return timestamp;
	}

	public ArrayList<ExtendedExchange> getExchanges() {
		return exchanges;
	}

	public ArrayList<ExtendedRouteInfo> getRoutes() {
		return routes;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public void setExchanges(ArrayList<ExtendedExchange> exchanges) {
		this.exchanges = exchanges;
	}

	public void setRoutes(ArrayList<ExtendedRouteInfo> routes) {
		this.routes = routes;
	}
}
