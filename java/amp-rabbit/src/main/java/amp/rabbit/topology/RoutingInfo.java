package amp.rabbit.topology;


import java.util.ArrayList;


public class RoutingInfo {

    protected ArrayList<RouteInfo> routes;

    public RoutingInfo() {
        this.routes = new ArrayList<RouteInfo>();
    }

    public RoutingInfo(ArrayList<RouteInfo> routes) {
        this.routes = routes;
    }

    public ArrayList<RouteInfo> getRoutes() {
        return routes;
    }

    public void setRoutes(ArrayList<RouteInfo> routes) {
        this.routes = routes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{ \"routes\" : [");
        for (RouteInfo route : this.routes) {
            sb.append(route.toString());
        }
        sb.append("]}");

        return sb.toString();
    }
}
