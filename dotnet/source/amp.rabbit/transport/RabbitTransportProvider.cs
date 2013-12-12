using System;
using System.Collections.Generic;
using System.Linq;
using amp.bus;
using amp.messaging;
using amp.rabbit.connection;
using amp.rabbit.topology;
using cmf.bus;
using Common.Logging;

namespace amp.rabbit.transport
{
    public class RabbitTransportProvider : ITransportProvider
    {
        public event Action<IEnvelopeDispatcher> OnEnvelopeReceived;

        private static readonly ILog Log = LogManager.GetLogger(typeof(RabbitTransportProvider));

        protected IDictionary<IRegistration, MultiConnectionRabbitReceiver> _listeners;
        protected ITopologyService _topoSvc;
        protected IConnectionManagerCache _connFactory;
        protected MultiConnectionRabbitSender _rabbitSender;
        protected IRoutingInfoCache _routingInfoCache;


        public RabbitTransportProvider(
            ITopologyService topologyService, 
            IRabbitConnectionFactory connFactory,
            IRoutingInfoCache routingInfoCache)
        {
            _topoSvc = topologyService;
            _connFactory = new ConnectionManagerCache(connFactory);
            _rabbitSender = new MultiConnectionRabbitSender(_connFactory);
            _routingInfoCache = routingInfoCache;

            _listeners = new Dictionary<IRegistration, MultiConnectionRabbitReceiver>();
        }


        public void Send(Envelope env)
        {
            Log.Debug("Enter Send");

            // first, get the topology based on the headers
            RoutingInfo routing = this.GetRoutingFromCacheOrService(_routingInfoCache, _topoSvc, env.Headers);

            _rabbitSender.Send(routing, env);
         
            Log.Debug("Leave Send");
        }

        public RoutingInfo GetRoutingFromCacheOrService(
            IRoutingInfoCache cache, 
            ITopologyService service, 
            IDictionary<string, string> hints)
        {
            // if there are no hints, we have no idea what to do
            if ((null == hints) || (!hints.Any()))
            {
                return null;
            }

            // pull the topic from the hints
            string topic = hints.GetMessageTopic();

            // first, check the cache
            RoutingInfo routing = cache.GetIfPresent(topic);

            // if nothing, use the service
            if (null == routing)
            {
                Log.Debug(string.Format("No routing information cache for {0}; using the topology service.", topic));

                routing = service.GetRoutingInfo(hints);

                // if we get something from the service, cache it
                if (null != routing)
                {
                    cache.Put(topic, routing);
                }
            }
            else
            {
                Log.Debug(string.Format("Routing information for {0} was found in the cache.", topic));
            }

            // whatever we end up with, return it
            return routing;
        }

        public void Register(IRegistration registration)
        {
            Log.Debug("Enter Register");

            // first, get the topology based on the registration info
            RoutingInfo routing = this.GetRoutingFromCacheOrService(_routingInfoCache, _topoSvc, registration.Info);

            var receiver = new MultiConnectionRabbitReceiver(_connFactory, routing, registration, listener_OnEnvelopeReceived);

            //TODO: Is this a good idea?  What if they register the same registration multiple times (easy way to do multi-threading...)  Just use a list instead!
            _listeners.Add(registration, receiver);


            Log.Debug("Leave Register");
        }

        public virtual void Unregister(IRegistration registration)
        {
            if (_listeners.ContainsKey(registration))
            {
                MultiConnectionRabbitReceiver listener = _listeners[registration];
                listener.StopListening();

                _listeners.Remove(registration);
            }
        }

        public void Dispose()
        {
            Dispose(true);
            GC.SuppressFinalize(this);
        }


        protected virtual void listener_OnEnvelopeReceived(IEnvelopeDispatcher dispatcher)
        {
            if (null != this.OnEnvelopeReceived)
            {
                foreach (Delegate d in this.OnEnvelopeReceived.GetInvocationList())
                {
                    try { d.DynamicInvoke(dispatcher); }
                    catch { }
                }
            }
        }

        protected virtual void Dispose(bool disposing)
        {
            Log.Debug("Enter Dispose");

            if (disposing)
            {
                // get rid of managed resources
                try { _listeners.Values.ToList().ForEach(l => l.Dispose()); }
                catch { }

                try { _connFactory.Dispose(); }
                catch { }

                try { _routingInfoCache.Dispose(); }
                catch { }
            }
            // get rid of unmanaged resources

            Log.Debug("Leave Dispose");
        }
    }
}
