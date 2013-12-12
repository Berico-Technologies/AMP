using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using System.Threading;
using amp.bus;
using amp.messaging;
using amp.rabbit.topology;
using cmf.bus;
using Common.Logging;
using RabbitMQ.Client;

namespace amp.rabbit.transport
{
    public class RabbitTransportProvider : ITransportProvider
    {
        public event Action<IEnvelopeDispatcher> OnEnvelopeReceived;

        private static readonly ILog Log = LogManager.GetLogger(typeof(RabbitTransportProvider));

        protected IDictionary<IRegistration, RabbitListener> _listeners;
        protected ITopologyService _topoSvc;
        protected IRabbitConnectionFactory _connFactory;
        protected IRoutingInfoCache _routingInfoCache;


        public RabbitTransportProvider(
            ITopologyService topologyService, 
            IRabbitConnectionFactory connFactory,
            IRoutingInfoCache routingInfoCache)
        {
            _topoSvc = topologyService;
            _connFactory = connFactory;
            _routingInfoCache = routingInfoCache;

            _listeners = new Dictionary<IRegistration, RabbitListener>();
        }


        public void Send(Envelope env)
        {
            Log.Debug("Enter Send");

            // first, get the topology based on the headers
            RoutingInfo routing = this.GetRoutingFromCacheOrService(_routingInfoCache, _topoSvc, env.Headers);

            // next, pull out all the producer exchanges
            IEnumerable<Exchange> exchanges =
                from route in routing.Routes
                select route.ProducerExchange;

            // for each exchange, send the envelope
            foreach (Exchange ex in exchanges)
            {
                Log.Debug("Sending to exchange: " + ex.ToString());
                IConnection conn = _connFactory.ConnectTo(ex);
                
                using (IModel channel = conn.CreateModel())
                {
                    IBasicProperties props = channel.CreateBasicProperties();
                    props.Headers = env.Headers as IDictionary;

                    channel.ExchangeDeclare(ex.Name, ex.ExchangeType, ex.IsDurable, ex.IsAutoDelete, ex.Arguments);
                    channel.BasicPublish(ex.Name, ex.RoutingKey, props, env.Payload);

                    // close the channel, but not the connection.  Channels are cheap.
                    channel.Close();
                }
            }

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

            // next, pull out all the consumer exchanges
            IEnumerable<Exchange> exchanges =
                from route in routing.Routes
                select route.ConsumerExchange;

            foreach (Exchange ex in exchanges)
            {
                IConnection conn = _connFactory.ConnectTo(ex);

                // create a listener
                RabbitListener listener = new RabbitListener(registration, ex, conn);
                listener.OnEnvelopeReceived += this.listener_OnEnvelopeReceived;
                listener.OnClose += _listeners.Remove;

                // put it on another thread so as not to block this one but
                // don't continue on this thread until we've started listening
                ManualResetEvent startEvent = new ManualResetEvent(false);
                Thread listenerThread = new Thread(listener.Start);
                listenerThread.Name = string.Format("{0} on {1}:{2}{3}", ex.QueueName, ex.HostName, ex.Port, ex.VirtualHost);
                listenerThread.Start(startEvent);

                // wait for the RabbitListener to start
                startEvent.WaitOne(new TimeSpan(0, 0, 30));

                // store the listener
                _listeners.Add(registration, listener);
            }

            Log.Debug("Leave Register");
        }

        public virtual void Unregister(IRegistration registration)
        {
            if (_listeners.ContainsKey(registration))
            {
                RabbitListener listener = _listeners[registration];
                listener.Stop();

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
                try { _listeners.Values.ToList().ForEach(l => l.Stop()); }
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
