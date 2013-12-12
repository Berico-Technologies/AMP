package amp.rabbit.transport;

import java.util.Map;

import amp.rabbit.connection.ConnectionManagerCache;
import amp.rabbit.connection.IConnectionManagerCache;
import amp.rabbit.connection.IRabbitConnectionFactory;
import amp.rabbit.topology.ITopologyService;
import amp.rabbit.topology.RoutingInfo;
import cmf.bus.Envelope;
import cmf.bus.IEnvelopeSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Created with IntelliJ IDEA.
 * User: jar349
 * Date: 5/1/13
 */
public class RabbitEnvelopeSender implements IEnvelopeSender {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitEnvelopeSender.class);

    private ITopologyService 					  _topologyService;    
    private MultiConnectionRabbitSender _rabbitSender;

    public RabbitEnvelopeSender(ITopologyService topologyService, IRabbitConnectionFactory connectionFactory) {

		this(topologyService, new ConnectionManagerCache(connectionFactory));
    }

    public RabbitEnvelopeSender(ITopologyService topologyService, Map<String, IRabbitConnectionFactory> connectionFactories) {

		this(topologyService, new ConnectionManagerCache(connectionFactories));
    }

    private RabbitEnvelopeSender(ITopologyService topologyService, IConnectionManagerCache connectionManagerCache) {

        _topologyService = topologyService;
        _rabbitSender = new MultiConnectionRabbitSender(connectionManagerCache);
    }

    @Override
    public void send(Envelope envelope) throws Exception {

        LOG.debug("Enter Send");

        // first, get the topology based on the headers
        RoutingInfo routing = _topologyService.getRoutingInfo(envelope.getHeaders());

        _rabbitSender.send(routing, envelope);
        LOG.debug("Leave Send");
    }
}
