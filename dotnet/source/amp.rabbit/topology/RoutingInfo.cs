using System.Collections.Generic;
using amp.utility;

namespace amp.rabbit.topology
{
    public class RoutingInfo
    {
        public IEnumerable<ProducingRoute> ProducingRoutes { get; private set; }
        public IEnumerable<ConsumingRoute> ConsumingRoutes { get; private set; }

        public RoutingInfo(IEnumerable<ProducingRoute> producingRoutes, IEnumerable<ConsumingRoute> consumingRoutes)
        {
            ProducingRoutes = producingRoutes;
            ConsumingRoutes = consumingRoutes;
        }

        protected bool Equals(RoutingInfo other)
        {
            return HashUtility.Compare(ProducingRoutes, other.ProducingRoutes) 
                && HashUtility.Compare(ConsumingRoutes, other.ConsumingRoutes);
        }

        public override bool Equals(object obj)
        {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            if (obj.GetType() != this.GetType()) return false;
            return Equals((RoutingInfo) obj);
        }

        public override int GetHashCode()
        {
            unchecked
            {
                return ((ProducingRoutes != null ? HashUtility.GetHashCode(ProducingRoutes) : 0)*397) 
                    ^ (ConsumingRoutes != null ? HashUtility.GetHashCode(ConsumingRoutes) : 0);
            }
        }
    }
}
