using System.Collections.Generic;

namespace amp.rabbit.topology
{
    public class ProducingRoute : BaseRoute
    {
        public ProducingRoute(IEnumerable<Broker> brokers, Exchange exchange, IEnumerable<string> routingKeys) 
            : base(brokers, exchange, routingKeys)
        {
        }

        public override string ToString()
        {
            return string.Format("ConsumingRoute [brokers={0}, exchange={1}, routingKey={2}]",
                Brokers, Exchange, RoutingKeys);
        }
    }
}