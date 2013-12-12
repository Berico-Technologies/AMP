package amp.rabbit.transport;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import amp.bus.IEnvelopeDispatcher;
import amp.bus.IEnvelopeReceivedCallback;
import cmf.bus.IEnvelopeReceiver;
import cmf.bus.IRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import amp.rabbit.connection.ConnectionManagerCache;
import amp.rabbit.connection.IConnectionManagerCache;
import amp.rabbit.connection.IRabbitConnectionFactory;
import amp.rabbit.topology.ITopologyService;
import amp.rabbit.topology.RoutingInfo;


/**
 * Created with IntelliJ IDEA.
 * User: jar349
 * Date: 5/1/13
 */
public class RabbitEnvelopeReceiver implements IEnvelopeReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitEnvelopeReceiver.class);

    private ITopologyService _topologyService;
    private IConnectionManagerCache _connectionFactory;
    private ConcurrentHashMap<IRegistration, MultiConnectionRabbitReceiver> _listeners;

    public RabbitEnvelopeReceiver(ITopologyService topologyService, IRabbitConnectionFactory connectionFactory) {

		this(topologyService, new ConnectionManagerCache(connectionFactory));
    }

    public RabbitEnvelopeReceiver(ITopologyService topologyService, Map<String, IRabbitConnectionFactory> connectionFactories) {

		this(topologyService, new ConnectionManagerCache(connectionFactories));
    }

    private RabbitEnvelopeReceiver(ITopologyService topologyService, IConnectionManagerCache connectionManagerCache) {

        _topologyService = topologyService;
        _connectionFactory = connectionManagerCache;

        _listeners = new ConcurrentHashMap<IRegistration, MultiConnectionRabbitReceiver>();
    }


    @Override
    public void register(IRegistration registration) throws Exception {

        LOG.debug("Enter Register");

        // first, get the topology based on the registration info
        RoutingInfo routing = _topologyService.getRoutingInfo(registration.getRegistrationInfo());

        // Create the envelope handler...
        IEnvelopeReceivedCallback handler= new IEnvelopeReceivedCallback() {
            @Override
            public void handleReceive(IEnvelopeDispatcher dispatcher) {
                LOG.debug("Got an envelope from the RabbitListener: dispatching.");
                // the dispatcher encapsulates the logic of giving the envelope to handlers
                dispatcher.dispatch();
            }};
            
        MultiConnectionRabbitReceiver receiver = 
        		new MultiConnectionRabbitReceiver(_connectionFactory,routing, registration,handler);
        
        // store the listener
        //TODO: Is this a good idea?  What if they register the same registration multiple times (easy way to do multi-threading...)  Just use a list instead!
        _listeners.put(registration, receiver);
 
        LOG.debug("Leave Register");
    }

    @Override
    public void unregister(IRegistration registration) throws Exception {
    	MultiConnectionRabbitReceiver receiver = _listeners.remove(registration);

        if (receiver != null) {
        	receiver.stopListening();
        }
    }

    @Override
    public void dispose() {

        for (MultiConnectionRabbitReceiver l : _listeners.values()) {

            try { l.dispose(); } catch (Exception ex) { }
        }

        try {  _topologyService.dispose(); } catch (Exception ex) { }

        try {  _connectionFactory.dispose(); } catch (Exception ex) { }
    }



          //TODO: >>  JM handle closing... 
//        listener.onClose(new IListenerCloseCallback() {
//
//            @Override
//            public void onClose(IRegistration registration) {
//
//                _listeners.remove(registration);
//            }
//        });

  


    /**
     * Called before the instance is recycled.
     */
    @Override
    protected void finalize() {
        this.dispose();
    }
}
