using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using amp.rabbit.topology;

namespace amp.topology.client
{
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
        public Hashtable Arguments { get; set; }


        public DefaultApplicationExchangeProvider()
        {
            this.ClientName = Guid.NewGuid().ToString();
            this.ExchangeName = "amp.fallback";
            this.Hostname = "rabbit";
            this.VHost = "/";
            this.Port = 5672;
            this.ExchangeType = "direct";
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
                    this.ExchangeName, this.Hostname, this.VHost,
                    this.Port, topic, oneQueueToRuleThemAll, this.ExchangeType,
                    this.IsDurable, this.IsAutoDelete, this.Arguments);

            // for both producing and consuming
            RouteInfo oneRouteToBringThemAll = new RouteInfo(oneExchangeToFindThem, oneExchangeToFindThem);

            // make a list out of the one route
            List<RouteInfo> andInTheDarknessBindThem = new RouteInfo[] { oneRouteToBringThemAll }.ToList();

            // and create routing info from it
            return new RoutingInfo(andInTheDarknessBindThem);
        }

        public void Dispose()
        {
        }
    }
}
