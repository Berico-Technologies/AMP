using System;
using System.Collections.Generic;
using amp.bus;
using amp.rabbit.connection;
using amp.rabbit.dispatch;
using amp.rabbit.topology;
using cmf.bus;
using Common.Logging;

namespace amp.rabbit.transport
{
    public class MultiConnectionRabbitReceiver : IDisposable
    {
        private readonly Action<IEnvelopeDispatcher> _handler;
        private static readonly ILog Log = LogManager.GetLogger(typeof(MultiConnectionRabbitReceiver));

        protected IList<RabbitListener> _listeners = new List<RabbitListener>();

        public MultiConnectionRabbitReceiver(IConnectionManagerCache connectionFactory, RoutingInfo routingInfo, IRegistration registration, Action<IEnvelopeDispatcher> handler)
        {
            _handler = handler;
            foreach (var route in routingInfo.ConsumingRoutes)
            {
                foreach (var channelProvider in connectionFactory.GetConnectionManagersFor(route))
                {
                    RabbitListener listener = createListener(registration, route, channelProvider);

                    // Important to add listener before starting just in-case StopListening is called in the mean time.
                    _listeners.Add(listener);

                    listener.Start();
                }
            }
        }

        protected RabbitListener createListener(IRegistration registration, ConsumingRoute route, IConnectionManager channelProvider)
        {
            // create a listener
            RabbitListener listener = new RabbitListener(registration, route, channelProvider);
            listener.OnEnvelopeReceived += _handler;
            
            //TODO: Resolve that RabbitListener does not implement OnConnectionError
            //listener.OnConnectionError(new ReconnectOnConnectionErrorCallback(_channelFactory));

            return listener;
        }


        public void StopListening()
        {
            foreach (var listener in _listeners)
            {
                try
                {
                    listener.Stop();
                }
                catch (Exception e)
                {
                    Log.Warn("Listener.Stop threw an exception.", e);
                }
            }
            _listeners.Clear();
        }

        public void Dispose()
        {
            StopListening();
        }
    }
}
