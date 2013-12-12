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
            else if (null == routing.Routes)
            {
                isNullOrEmpty = true;
            }
            else if (!routing.Routes.Any())
            {
                isNullOrEmpty = true;
            }

            return isNullOrEmpty;
        }
    }
}
