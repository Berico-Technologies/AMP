using System.Collections.Generic;

namespace amp.rabbit.topology
{
    public class RoutingInfo
    {
        public IEnumerable<RouteInfo> Routes { get; set; }


        public RoutingInfo()
        {
            this.Routes = new List<RouteInfo>();
        }

        public RoutingInfo(IEnumerable<RouteInfo> routes)
        {
            this.Routes = routes;
        }
    }
}
