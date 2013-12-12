package amp.rabbit.transport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cmf.bus.Envelope;
import amp.messaging.EnvelopeHeaderConstants;
import cmf.bus.IRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import amp.bus.IEnvelopeDispatcher;
import amp.bus.IEnvelopeReceivedCallback;
import amp.bus.ITransportProvider;
import amp.rabbit.connection.ConnectionManagerCache;
import amp.rabbit.connection.IConnectionManagerCache;
import amp.rabbit.connection.IRabbitConnectionFactory;
import amp.rabbit.topology.ITopologyService;
import amp.rabbit.topology.RoutingInfo;

/**
 * AMQP Implementation of the Transport Provider using the RabbitMQ Client.
 * 
 * @author rclayton
 */
public class RabbitTransportProvider implements ITransportProvider {

	private static final Logger LOG = LoggerFactory
			.getLogger(RabbitTransportProvider.class);

	/** Toplogy service to get the routing information */
	protected ITopologyService topologyService;
	
	/** cache for routing info (from topology service) */
	protected IRoutingInfoCache routingInfoCache;
	
	protected IConnectionManagerCache connectionFactory;
	
	/** These are the channel/queue listeners that actually receive events. */
	protected ConcurrentHashMap<IRegistration, MultiConnectionRabbitReceiver> listenerMap = 
			new ConcurrentHashMap<IRegistration, MultiConnectionRabbitReceiver>();
	
	/** RabbitTransportProvider clients add their envelope callbacks here. */
	protected List<IEnvelopeReceivedCallback> envCallbacks = new ArrayList<IEnvelopeReceivedCallback>();	
	
	protected MultiConnectionRabbitSender rabbitSender;
	
	/**
	 * Initialize the Transport Provider with the Topology Service and a default
	 * Connection Factory.
	 * 
	 * @param topologyService
	 *            Service that determines the correct exchange and broker to
	 *            send messages to.
	 * @param connectionFactory
	 *            Service that uses topology information to establish
	 *            connections to the broker.
	 */
	public RabbitTransportProvider(ITopologyService topologyService,
			IRabbitConnectionFactory connectionFactory,
			IRoutingInfoCache routingInfoCache) {

		this(topologyService, 
				new ConnectionManagerCache(connectionFactory), 
				routingInfoCache);
	}

	/**
	 * Initialize the Transport Provider with the Topology Service and a default
	 * Connection Factory.
	 * 
	 * @param topologyService
	 *            Service that determines the correct exchange and broker to
	 *            send messages to.
	 * @param connectionFactory
	 *            Service that uses topology information to establish
	 *            connections to the broker.
	 */
	public RabbitTransportProvider(ITopologyService topologyService,
			Map<String, IRabbitConnectionFactory> connectionFactories,
			IRoutingInfoCache routingInfoCache) {
		
		this(topologyService, 
			new ConnectionManagerCache(connectionFactories), 
			routingInfoCache);
	}

	private RabbitTransportProvider(ITopologyService topologyService,
			IConnectionManagerCache connectionManagerCache,
			IRoutingInfoCache routingInfoCache) {

		this.topologyService = topologyService;
		this.connectionFactory = connectionManagerCache;
		this.routingInfoCache = routingInfoCache;
		this.rabbitSender = new MultiConnectionRabbitSender(connectionManagerCache);
	}

	/**
	 * Register a new Envelope handler for the specified routes.
	 * 
	 * @param registration
	 *            Handlers and routing hints.
	 */
	@Override
	public void register(IRegistration registration) throws Exception {

		LOG.debug("Enter Register");

		// first, get the topology based on the registration info
		RoutingInfo routing = this.getRoutingFromCacheOrService(
				routingInfoCache, topologyService,
				registration.getRegistrationInfo());

		// next, pull out all the producer exchanges
		IEnvelopeReceivedCallback handler = new IEnvelopeReceivedCallback() {

			@Override
			public void handleReceive(IEnvelopeDispatcher dispatcher) {
                raise_onEnvelopeReceivedEvent(dispatcher);
			}};
		
		MultiConnectionRabbitReceiver receiver = 
				new MultiConnectionRabbitReceiver(connectionFactory,routing,registration,handler);
		
        //TODO: Is this a good idea?  What if they register the same registration multiple times (easy way to do multi-threading...)  Just use a list instead!
		listenerMap.put(registration, receiver);
		
		LOG.debug("Leave Register");
	}

	@Override
	public void send(Envelope env) throws Exception {

		LOG.debug("Enter Send");

		// first, get the topology based on the headers
		RoutingInfo routing = this.getRoutingFromCacheOrService(
				routingInfoCache, topologyService, env.getHeaders());

		rabbitSender.send(routing, env);

		LOG.debug("Leave Send");
	}

	/**
	 * Unregister a registration with the bus (canceling the listener and
	 * stopping consumption from the broker).
	 * 
	 * @param registration
	 *            The original registration used to create the listener.
	 */
	@Override
	public void unregister(IRegistration registration) {

		MultiConnectionRabbitReceiver receiver = listenerMap.remove(registration);
		if (receiver != null) {
			receiver.stopListening();
		}
	}

	/**
	 * Register a callback for the EnvelopeReceived event.
	 * 
	 * @param callback
	 *            Called when an envelope is received.
	 */
	@Override
	public void onEnvelopeReceived(IEnvelopeReceivedCallback callback) {
		envCallbacks.add(callback);
	}

	/**
	 * Gets routing info - based on routing hints - from the cache if present,
	 * or the service if not.
	 * 
	 * @param cache
	 *            the cache to check for routing info
	 * @param service
	 *            the service to use if routing isn't cached
	 * @param hints
	 *            the routing hints that specify routing info
	 * @return RoutingInfo, or null
	 */
	public RoutingInfo getRoutingFromCacheOrService(IRoutingInfoCache cache,
			ITopologyService service, Map<String, String> hints) {

		// if there are no hints, we have no idea what to do
		if ((null == hints) || (hints.isEmpty())) {
			return null;
		}

		// pull the topic from the hints
		String topic = hints.get(EnvelopeHeaderConstants.MESSAGE_TOPIC);

		// first, check the cache
		RoutingInfo routing = cache.getIfPresent(topic);

		// if nothing, use the service
		if (null == routing) {
			LOG.debug(
					"No routing information cached for {}; using the topology service.",
					topic);
			routing = service.getRoutingInfo(hints);

			// if we got something from the service, cache it
			if (null != routing) {
				cache.put(topic, routing);
			}
		} else {
			LOG.debug("Routing information for {} was found in the cache.",
					topic);
		}

		// whatever we end up with, return it
		return routing;
	}

	
	
	/**
	 * Gracefully shutdown all the services associated with the Transport
	 * Provider.
	 */
	@Override
	public void dispose() {

		try {
			connectionFactory.dispose();
		} catch (Exception ex) {
		}

		try {
			topologyService.dispose();
		} catch (Exception ex) {
		}

		for (MultiConnectionRabbitReceiver receiver : listenerMap.values()) {
			try {
				receiver.dispose();
			} catch (Exception ex) {
			}
		}
		rabbitSender.dispose();
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