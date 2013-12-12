using amp.rabbit.connection;
using amp.rabbit.topology;
using cmf.bus;
using Common.Logging;

namespace amp.rabbit.transport
{
    public class RabbitEnvelopeSender : IEnvelopeSender
    {
        private static readonly ILog Log = LogManager.GetLogger(typeof(RabbitEnvelopeSender));

        protected ITopologyService _topologyService;
        protected MultiConnectionRabbitSender _rabbitSender;

        public RabbitEnvelopeSender(
            ITopologyService topologyService, 
            IRabbitConnectionFactory connFactory)
        {
            _topologyService = topologyService;
            _rabbitSender = new MultiConnectionRabbitSender(new ConnectionManagerCache(connFactory)); 
        }

        public void Send(Envelope envelope)
        {
            Log.Debug("Enter Send");

            // first, get the topology based on the headers
            RoutingInfo routing = _topologyService.GetRoutingInfo(envelope.Headers);

            _rabbitSender.Send(routing, envelope);
            
            Log.Debug("Leave Send");        
        }

        public void Dispose()
        {
            _topologyService.Dispose();
            _rabbitSender.Dispose();
        }
    }
}
