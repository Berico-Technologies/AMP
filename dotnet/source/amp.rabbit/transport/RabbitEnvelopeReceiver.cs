using System.Collections.Generic;
using amp.rabbit.connection;
using amp.rabbit.topology;
using cmf.bus;
using Common.Logging;

namespace amp.rabbit.transport
{
    public class RabbitEnvelopeReceiver : IEnvelopeReceiver
    {
        private static readonly ILog Log = LogManager.GetLogger(typeof(RabbitEnvelopeReceiver));

        protected IDictionary<IRegistration, MultiConnectionRabbitReceiver> _listeners;
        protected ITopologyService _topologyService;
        protected IConnectionManagerCache _connFactory;

        public RabbitEnvelopeReceiver(
            ITopologyService topologyService, 
            IRabbitConnectionFactory connFactory)
        {
            _topologyService = topologyService;
            _connFactory = new ConnectionManagerCache(connFactory);

            _listeners = new Dictionary<IRegistration, MultiConnectionRabbitReceiver>();
        }

        public void Register(IRegistration registration)
        {
            Log.Debug("Enter Register");

            // first, get the topology based on the registration info
            RoutingInfo routing = _topologyService.GetRoutingInfo(registration.Info);

            var receiver = new MultiConnectionRabbitReceiver(_connFactory, routing, registration, dispatcher =>
            {
                Log.Debug("Got an envelope from the RabbitListener: dispatching.");
                // the dispatcher encapsulates the logic of giving the envelope to handlers
                dispatcher.Dispatch();
            });

            //TODO: Is this a good idea?  What if they register the same registration multiple times (easy way to do multi-threading...)  Just use a list instead!
            _listeners.Add(registration, receiver);

            Log.Debug("Leave Register");        
        }

        public void Unregister(IRegistration registration)
        {
            MultiConnectionRabbitReceiver listener;

            if (_listeners.TryGetValue(registration, out listener))
            {
                try
                {
                    listener.StopListening();
                    _listeners.Remove(registration);
                } catch { }
            }
        }

        public void Dispose()
        {
            foreach (MultiConnectionRabbitReceiver listener in _listeners.Values) 
            {
                try { listener.Dispose(); } catch { }
            }

            _topologyService.Dispose();
            _connFactory.Dispose();
        }
    }
}
