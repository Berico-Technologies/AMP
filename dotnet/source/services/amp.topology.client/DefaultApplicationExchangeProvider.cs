using System;
using System.Collections.Generic;
using amp.rabbit.topology;
using Queue = amp.rabbit.topology.Queue;

namespace amp.topology.client
{
    //TODO: inherit from SimpleTopologyService as in Java Code?
    /// <summary>
    /// Provides a route on the "amq.direct" exchange (which may only be a RabbitMQ     
    /// construct).  The implementation will not provide a queue name, assuming the     
    /// transport will create a unique queue instead (we don't want round-robin on      
    /// a single queue).  The exchange provide is "direct" by default, meaning delivery 
    /// of messages will require an exact match (that is, routing key = topic). You will
    /// not be able to consume multiple event types on the same queue unless you change 
    /// the default AMP implementation.                                                 
    /// </summary>
    /// <author>John Ruiz (Berico Technologies)</author>
    public class DefaultApplicationExchangeProvider : IFallbackRoutingInfoProvider
    {
        private static long UniqueProcessId = 0;

        public string ClientName { get; set; }
        public string ExchangeName { get; set; }
        public string Hostname { get; set; }
        public string VHost { get; set; }
        public int Port { get; set; }
        public string ExchangeType { get; set; }
        public string QueueName { get; set; }
        public bool IsDurable { get; set; }
        public bool IsAutoDelete { get; set; }
        public Dictionary<string,object> Arguments { get; set; }


        public DefaultApplicationExchangeProvider()
        {
            this.ClientName = Guid.NewGuid().ToString();
            this.ExchangeName = "amp.simple";
            this.Hostname = "rabbit";
            this.VHost = "/";
            this.Port = 5672;
            this.ExchangeType = "topic";
            this.QueueName = null;
            this.IsDurable = false;
            this.IsAutoDelete = true;
        }


        public RoutingInfo GetFallbackRoute(string topic)
        {
            // either use the given named queue, or build one dynamically
            string oneQueueToRuleThemAll = this.QueueName ?? string.Format(
                "{0}#{1}#{2}", this.ClientName, ++UniqueProcessId, topic);

            // there's only one exchange
            Exchange oneExchangeToFindThem = new Exchange(
                    this.ExchangeName, exchangeType: this.ExchangeType,
                    isDurable: this.IsDurable, autoDelete: this.IsAutoDelete, arguments: this.Arguments);
 
            ProducingRoute producingRoute = new ProducingRoute(
                new[] { new Broker(this.Hostname, this.Port) },
                oneExchangeToFindThem,
                new[] { topic });

            ConsumingRoute consumingRoute = new ConsumingRoute(
                new[] { new Broker(this.Hostname, this.Port) },
                oneExchangeToFindThem,
                new Queue(oneQueueToRuleThemAll, this.IsAutoDelete, this.IsDurable, true, true, null),
                new[] { topic });

            return new RoutingInfo(new[] { producingRoute }, new[] { consumingRoute });
        }

        public void Dispose()
        {
        }
    }
}
