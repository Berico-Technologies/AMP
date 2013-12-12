using System.Collections.Generic;

using amp.utility;

namespace amp.rabbit.topology
{
    public abstract class BaseRoute
    {
        public IEnumerable<Broker> Brokers { get; private set; }
        public Exchange Exchange { get; private set; }
        public IEnumerable<string> RoutingKeys { get; private set; }

        protected BaseRoute(IEnumerable<Broker> brokers, Exchange exchange,
            IEnumerable<string> routingKeys)
        {
            Brokers = brokers;
            Exchange = exchange;
            RoutingKeys = routingKeys;
        }

        protected bool Equals(BaseRoute other)
        {
            return HashUtility.Compare(Brokers, other.Brokers) 
                && Exchange.Equals(other.Exchange)
                && HashUtility.Compare(RoutingKeys, other.RoutingKeys);
        }

        public override bool Equals(object obj)
        {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            if (obj.GetType() != this.GetType()) return false;
            return Equals((BaseRoute) obj);
        }

        public override int GetHashCode()
        {
            unchecked
            {
                var hashCode = (Brokers != null ? HashUtility.GetHashCode(Brokers) : 0);
                hashCode = (hashCode*397) ^ Exchange.GetHashCode();
                hashCode = (hashCode*397) ^ (RoutingKeys != null ? HashUtility.GetHashCode(RoutingKeys) : 0);
                return hashCode;
            }
        }
    }
}
