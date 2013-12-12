using System.Collections.Generic;

namespace amp.rabbit.topology
{
    public class ConsumingRoute : BaseRoute
    {
        public Queue Queue { get; private set; }

        public ConsumingRoute(IEnumerable<Broker> brokers, Exchange exchange, Queue queue, IEnumerable<string> routingKeys)
            : base(brokers, exchange, routingKeys)
        {
            Queue = queue;
        }

        public override string ToString()
        {
            return string.Format("ConsumingRoute [queue={0}, brokers={1}, exchange={2}, routingKey={3}]", 
                Queue, Brokers, Exchange, RoutingKeys);
        }

        protected bool Equals(ConsumingRoute other)
        {
            return base.Equals(other) 
                   && Equals(Queue, other.Queue);
        }

        public override bool Equals(object obj)
        {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            if (obj.GetType() != this.GetType()) return false;
            return Equals((ConsumingRoute) obj);
        }

        public override int GetHashCode()
        {
            unchecked
            {
                return (base.GetHashCode()*397) ^ (Queue != null ? Queue.GetHashCode() : 0);
            }
        }
    }
}