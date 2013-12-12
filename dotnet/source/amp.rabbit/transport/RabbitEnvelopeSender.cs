using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
using amp.rabbit.topology;
using cmf.bus;
using Common.Logging;
using RabbitMQ.Client;

namespace amp.rabbit.transport
{
    public class RabbitEnvelopeSender : IEnvelopeSender
    {
        private static readonly ILog Log = LogManager.GetLogger(typeof(RabbitEnvelopeSender));

        protected ITopologyService _topologyService;
        protected IRabbitConnectionFactory _connFactory;

        public RabbitEnvelopeSender(
            ITopologyService topologyService, 
            IRabbitConnectionFactory connFactory)
        {
            _topologyService = topologyService;
            _connFactory = connFactory;
        }

        public void Send(Envelope envelope)
        {
            Log.Debug("Enter Send");

            // first, get the topology based on the headers
            RoutingInfo routing = _topologyService.GetRoutingInfo(envelope.Headers);

            // next, pull out all the producer exchanges
            IEnumerable<Exchange> exchanges =
                from route in routing.Routes
                select route.ProducerExchange;

            // for each exchange, send the envelope
            foreach (Exchange ex in exchanges)
            {
                Log.Info("Sending to exchange: " + ex.ToString());

                try {
                    IConnection conn = _connFactory.ConnectTo(ex);

                    using (IModel channel = conn.CreateModel())
                    {
                        IBasicProperties props = channel.CreateBasicProperties();
                        props.Headers = envelope.Headers as IDictionary;

                        channel.ExchangeDeclare(ex.Name, ex.ExchangeType, ex.IsDurable, ex.IsAutoDelete, ex.Arguments);
                        channel.BasicPublish(ex.Name, ex.RoutingKey, props, envelope.Payload);

                        // close the channel, but not the connection.  Channels are cheap.
                        channel.Close();
                    }

                } catch (Exception e) {
                    Log.Error("Failed to send an envelope", e);
                    throw e;
                }
            }

            Log.Debug("Leave Send");        
        }

        public void Dispose()
        {
            _topologyService.Dispose();
            _connFactory.Dispose();
        }
    }
}
