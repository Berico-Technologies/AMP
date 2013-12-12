using System;
using System.Collections.Generic;
using amp.messaging;

namespace amp.rabbit.topology
{
    public class SimpleTopologyService : ITopologyService
    {
        public string ClientProfile { get; protected set; }
        public string Name { get; protected set; }
        public string Hostname { get; protected set; }
        public string VirtualHost { get; protected set; }
        public int Port { get; protected set; }


        public SimpleTopologyService(string clientProfile, string name, string hostname, string vhost, int port)
        {
            this.ClientProfile = string.IsNullOrEmpty(clientProfile) ? Guid.NewGuid().ToString() : clientProfile;
            this.Name = string.IsNullOrEmpty(name) ? "cmf.simple.exchange" : name;
            this.Hostname = string.IsNullOrEmpty(hostname) ? "localhost" : hostname;
            this.VirtualHost = string.IsNullOrEmpty(vhost) ? "/" : vhost;
            this.Port = (port == 0) ? 5672 : port;
        }


        public RoutingInfo GetRoutingInfo(IDictionary<string, string> routingHints)
        {
            string topic = routingHints.GetMessageTopic();
        
            Exchange theOneExchange = new Exchange(
                this.Name,
                exchangeType: "direct", 
                isDurable: false, 
                autoDelete: true);

            ProducingRoute producingRoute = new ProducingRoute(
                new[]{new Broker(this.Hostname, this.Port)},
                theOneExchange,
                new[]{ topic });

            ConsumingRoute consumingRoute = new ConsumingRoute(
                new[]{new Broker(this.Hostname, this.Port)},
                theOneExchange,
                new Queue(string.Format("{0}#{1}", this.ClientProfile, topic), true, false, true, true, null), 
                new[]{ topic });

            return new RoutingInfo(new[]{producingRoute}, new []{consumingRoute});
        }

        public void Dispose()
        {
        }
    }
}
