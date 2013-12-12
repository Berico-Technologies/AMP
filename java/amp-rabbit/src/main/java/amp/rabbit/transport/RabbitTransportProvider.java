package amp.rabbit.transport;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import amp.rabbit.IListenerCloseCallback;
import amp.rabbit.RabbitListener;
import amp.rabbit.ReconnectOnConnectionErrorCallback;
import cmf.bus.Envelope;
import cmf.bus.EnvelopeHeaderConstants;
import cmf.bus.IRegistration;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import amp.bus.IEnvelopeDispatcher;
import amp.bus.IEnvelopeReceivedCallback;
import amp.bus.ITransportProvider;
import amp.rabbit.IRabbitChannelFactory;
import amp.rabbit.topology.Exchange;
import amp.rabbit.topology.ITopologyService;
import amp.rabbit.topology.RouteInfo;
import amp.rabbit.topology.RoutingInfo;


/**
 * AMQP Implementation of the Transport Provider using the RabbitMQ Client.
 * 
 * @author rclayton
 */
public class RabbitTransportProvider implements ITransportProvider {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitTransportProvider.class);

    protected IRabbitChannelFactory channelFactory;
    protected List<IEnvelopeReceivedCallback> envCallbacks = new ArrayList<IEnvelopeReceivedCallback>();
    protected ConcurrentHashMap<IRegistration, RabbitListener> listeners = new ConcurrentHashMap<IRegistration, RabbitListener>();
    protected ITopologyService topologyService;
    protected IRoutingInfoCache routingInfoCache;



    /**
     * Initialize the Transport Provider with the Topology Service and the Channel Factory.
     * @param topologyService Service that determines the correct exchange and broker to send messages to.
     * @param channelFactory Service that uses topology information to establish connections to the broker.
     */
    public RabbitTransportProvider(
            ITopologyService topologyService,
            IRabbitChannelFactory channelFactory,
            IRoutingInfoCache routingInfoCache) {
	
		this.topologyService = topologyService;
		this.channelFactory = channelFactory;
        this.routingInfoCache = routingInfoCache;
    }



    /**
     * Register a new Envelope handler for the specified routes.
     * @param registration Handlers and routing hints.
     */
    @Override
    public void register(IRegistration registration) throws Exception {
    		
        LOG.debug("Enter Register");

        // first, get the topology based on the registration info
        RoutingInfo routing = this.getRoutingFromCacheOrService(
                routingInfoCache,
                topologyService,
                registration.getRegistrationInfo());

        // next, pull out all the producer exchanges
        List<Exchange> exchanges = new ArrayList<Exchange>();
        
        for (RouteInfo route : routing.getRoutes()) {
        
            exchanges.add(route.getConsumerExchange());
        }

        for (Exchange exchange : exchanges) {
        		
            RabbitListener listener = createListener(registration, exchange);

            // store the listener
            listeners.put(registration, listener);
        }

        LOG.debug("Leave Register");
    }

    @Override
    @SuppressWarnings({ "deprecation", "unchecked" })
    public void send(Envelope env) throws Exception {

        LOG.debug("Enter Send");

        // first, get the topology based on the headers
        RoutingInfo routing = this.getRoutingFromCacheOrService(routingInfoCache, topologyService, env.getHeaders());

        // next, pull out all the producer exchanges
        List<Exchange> exchanges = new ArrayList<Exchange>();

        for (RouteInfo route : routing.getRoutes()) {

            exchanges.add(route.getProducerExchange());
        }

        // for each exchange, send the envelope
        for (Exchange ex : exchanges) {
            LOG.info("Sending to exchange: " + ex.toString());

            Channel channel = null;

            try {
                channel = channelFactory.getChannelFor(ex);

                BasicProperties props = new BasicProperties.Builder().build();

                Map<String, Object> headers = new HashMap<String, Object>();

                for (Entry<String, String> entry : env.getHeaders().entrySet()) {

                    headers.put(entry.getKey(), entry.getValue());
                }

                props.setHeaders(headers);

                channel.exchangeDeclare(
                        ex.getName(), ex.getExchangeType(), ex.getIsDurable(),
                        ex.getIsAutoDelete(), ex.getArguments());

                channel.basicPublish(ex.getName(), ex.getRoutingKey(), props, env.getPayload());

            } catch (Exception e) {
                LOG.error("Failed to send an envelope", e);
                throw e;
            }
        }

        LOG.debug("Leave Send");
    }

    /**
     * Unregister a registration with the bus (canceling the listener
     * and stopping consumption from the broker).
     * @param registration The original registration used to create
     * the listener.
     */
    @Override
    public void unregister(IRegistration registration) {

        RabbitListener listener = listeners.remove(registration);

        if (listener != null) {

            listener.stopListening();
        }
    }

    /**
     * Register a callback for the EnvelopeReceived event.
     * @param callback Called when an envelope is received.
     */
    @Override
    public void onEnvelopeReceived(IEnvelopeReceivedCallback callback) {
        envCallbacks.add(callback);
    }

    /**
     * Gets routing info - based on routing hints - from the cache if present, or the service if not.
     * @param cache the cache to check for routing info
     * @param service the service to use if routing isn't cached
     * @param hints the routing hints that specify routing info
     * @return RoutingInfo, or null
     */
    public RoutingInfo getRoutingFromCacheOrService(IRoutingInfoCache cache, ITopologyService service, Map<String, String> hints) {

        // if there are no hints, we have no idea what to do
        if ( (null == hints) || (hints.isEmpty())) { return null; }

        // pull the topic from the hints
        String topic = hints.get(EnvelopeHeaderConstants.MESSAGE_TOPIC);

        // first, check the cache
        RoutingInfo routing = cache.getIfPresent(topic);

        // if nothing, use the service
        if (null == routing) {
            LOG.debug("No routing information cached for {}; using the topology service.", topic);
            routing = service.getRoutingInfo(hints);

            // if we got something from the service, cache it
            if (null != routing) {
                cache.put(topic, routing);
            }
        }
        else {
            LOG.debug("Routing information for {} was found in the cache.", topic);
        }

        // whatever we end up with, return it
        return routing;
    }

    /**
     * Gracefully shutdown all the services associated with the Transport Provider.
     */
    @Override
    public void dispose() {

        try {  channelFactory.dispose(); } catch (Exception ex) { }

        try {  topologyService.dispose(); } catch (Exception ex) { }

        for (RabbitListener l : listeners.values()) {

            try { l.dispose(); } catch (Exception ex) { }
        }
    }



    protected RabbitListener createListener(
			IRegistration registration, Exchange exchange) throws Exception {
    	
        // create a channel
        Channel channel = channelFactory.getChannelFor(exchange);

        // create a listener
        RabbitListener listener = this.getListener(registration, exchange);

        // hook into the listener's events
        listener.onEnvelopeReceived(new IEnvelopeReceivedCallback() {

            @Override
            public void handleReceive(IEnvelopeDispatcher dispatcher) {
            	
                raise_onEnvelopeReceivedEvent(dispatcher);
            }
        });
        
        listener.onClose(new IListenerCloseCallback() {

            @Override
            public void onClose(IRegistration registration) {
            	
                listeners.remove(registration);
            }
        });
        
        listener.onConnectionError(new ReconnectOnConnectionErrorCallback(channelFactory));

        listener.start(channel);
    		
        return listener;
    }
    
    /**
     * Get a new Rabbit Listener for the provided registration and exchange.
     * This was pulled out as an extension point for deriving classes, as well as,
     * to make testing a little easier.
     * 
     * @param registration Handlers and hints
     * @param exchange Routing Information
     * @return Listener that will pull messages from the broker and call the handlers
     * on the registration.
     */
    protected RabbitListener getListener(IRegistration registration, Exchange exchange) {
    		
        return new RabbitListener(registration, exchange);
    }

    /**
     * Used to signal EnvelopeReceived listeners that a new envelope has been
     * received from a listener.
     * @param dispatcher Object used to grab the envelope as well as to complete
     * the process of receiving the message.
     */
    protected void raise_onEnvelopeReceivedEvent(IEnvelopeDispatcher dispatcher) {
        for (IEnvelopeReceivedCallback callback : envCallbacks) {
            try {
                callback.handleReceive(dispatcher);
            } catch (Exception ex) {
                LOG.error("Caught an unhandled exception raising the onEnvelopeReceived event", ex);
            }
        }
    }

    /**
     * Called before the instance is recycled.
     */
    @Override
    protected void finalize() {
        dispose();
    }
}