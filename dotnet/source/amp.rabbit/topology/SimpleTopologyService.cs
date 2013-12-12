using System;
using System.Collections.Generic;
using amp.messaging;
using cmf.bus;

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
                this.Hostname, 
                this.VirtualHost, 
                this.Port,
                topic, 
                string.Format("{0}#{1}", this.ClientProfile, topic),
                "direct", 
                false, 
                true);

            RouteInfo theOneRoute = new RouteInfo(theOneExchange, theOneExchange);

            return new RoutingInfo(new RouteInfo[] { theOneRoute });
        }

        public void Dispose()
        {
        }
    }
}
