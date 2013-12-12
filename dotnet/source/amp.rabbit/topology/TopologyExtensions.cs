using System.Linq;

namespace amp.rabbit.topology
{
    public static class TopologyExtensions
    {
        public static bool IsNullOrEmpty(this RoutingInfo routing)
        {
            bool isNullOrEmpty = false;

            if (null == routing)
            {
                isNullOrEmpty = true;
            }
            else if (null == routing.ProducingRoutes)
            {
                isNullOrEmpty = true;
            }
            else if (!routing.ProducingRoutes.Any())
            {
                isNullOrEmpty = true;
            }
            else if (null == routing.ConsumingRoutes)
            {
                isNullOrEmpty = true;
            }
            else if (!routing.ConsumingRoutes.Any())
            {
                isNullOrEmpty = true;
            }

            return isNullOrEmpty;
        }
    }
}
