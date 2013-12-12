using System;
using System.Collections;
using System.Collections.Generic;
using amp.rabbit.connection;
using amp.rabbit.topology;
using cmf.bus;
using Common.Logging;
using RabbitMQ.Client;

namespace amp.rabbit.transport
{
    public class MultiConnectionRabbitSender : IDisposable
    {
        private static readonly ILog Log = LogManager.GetLogger(typeof(MultiConnectionRabbitSender));

        protected IConnectionManagerCache _connectionManagerCache;

        public MultiConnectionRabbitSender(IConnectionManagerCache connectionManagerCache)
        {
            _connectionManagerCache = connectionManagerCache;
        }

        public void Send(RoutingInfo routing, Envelope envelope)
        {
            Log.Debug("Enter Send");

            // for each exchange, send the envelope
            foreach (ProducingRoute route in routing.ProducingRoutes)
            {
                Log.Info("Sending to exchange: " + route.Exchange.ToString());

                //TODO: There is a problem here in how exceptions are being logged/rethrown:
                //It is possible for a envelope to go out on some exchanges and/or routes and then not others.
                //A failure aborts attempts at subequent exchanges/routes but does not role back previous.
                //Seems we should either continue to attempt later routes and throw at the end or role back.
                try {
                    IEnumerable<IConnectionManager> connectionManagers = _connectionManagerCache.GetConnectionManagersFor(route);

                    foreach (var connectionManager in connectionManagers)
                    {
                        using (IModel channel = connectionManager.CreateModel())
                        {
                            SendDataOnChannel(channel, route, envelope);
                        }
                    }

                } catch (Exception e) {
                    Log.Error("Failed to send an envelope", e);
                    throw;
                }
            }

            Log.Debug("Leave Send");        
        }

        protected static void SendDataOnChannel(IModel channel, ProducingRoute route, Envelope envelope)
        {
            IBasicProperties props = channel.CreateBasicProperties();
            props.Headers = envelope.Headers as IDictionary;

            Exchange ex = route.Exchange;
            if (ex.ShouldDeclare)
            {
                channel.ExchangeDeclare(ex.Name, ex.ExchangeType, ex.IsDurable, ex.IsAutoDelete, ex.ArgumentsAsDictionary);
            }

            foreach (string routingKey in route.RoutingKeys)
            {
                try
                {
                    channel.BasicPublish(ex.Name, routingKey, props, envelope.Payload);
                }
                catch (Exception e)
                {
                    Log.Error(
                        string.Format("Failed to send an envelope to route: {0} on excange {1}.",
                            routingKey, ex), e);
                    throw;
                }
            }

            // close the channel, but not the connection.  Channels are cheap.
            channel.Close();
        }

        public void Dispose()
        {
            _connectionManagerCache.Dispose();
        }
    }
}
