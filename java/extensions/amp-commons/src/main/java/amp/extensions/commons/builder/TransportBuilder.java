package amp.extensions.commons.builder;

import amp.bus.ITransportProvider;
import amp.commanding.DefaultCommandReceiver;
import amp.commanding.ICommandReceiver;
import amp.rabbit.IRabbitChannelFactory;
import amp.rabbit.topology.ITopologyService;
import amp.rabbit.transport.CommandableCache;
import amp.rabbit.transport.IRoutingInfoCache;
import amp.rabbit.transport.RabbitEnvelopeReceiver;
import amp.rabbit.transport.RabbitTransportProvider;

/**
 * A little fluent interface for configuring the
 * Transport Provider used with CMF+AMP.
 * 
 * @author Richard Clayton (Berico Technologies)
 */
public class TransportBuilder extends FluentExtension {

    //-- Fields --//
	ITopologyService topologyService = null;
	IRabbitChannelFactory channelFactory = null;
	ITransportProvider transportProvider = null;
    IRoutingInfoCache routingInfoCache = null;



    //-- Properties --//
    /**
     * Set the Topology Service.
     * @param topologyService Topology Service to use.
     */
    public void setTopologyService(ITopologyService topologyService){

        this.topologyService = topologyService;
    }

    /**
     * Set the Channel Factory.
     * @param channelFactory Channel Factory to use.
     */
    public void setChannelFactory(IRabbitChannelFactory channelFactory){

        this.channelFactory = channelFactory;
    }

    /**
     * Set the Transport Provider (do this if you enjoy pain!).
     * @param transportProvider Transport Provider to use.
     */
    public void setTransportProvider(ITransportProvider transportProvider){

        this.transportProvider = transportProvider;
    }

    /**
     * Set the Routing Info Cache
     * @param routingInfoCache
     */
    public void setRoutingInfoCache(IRoutingInfoCache routingInfoCache) {

        this.routingInfoCache = routingInfoCache;
    }



    //-- Constructors --//
    /**
	 * Initialize the builder with a reference to the parent fluent interface.
	 * @param parent Parent builder.
	 */
	public TransportBuilder(BusBuilder parent) {
	
		super(parent);
	}



    //-- Public Methods --/
	/**
	 * Select and configure the topology to use.
	 * @return Topology Builder interface.
	 */
	public TopologyBuilder routeWith(){
		
		return new TopologyBuilder(this.parent, this);
	}
	
	/**
	 * Select the Channel Factory to use.
	 * @return Channel Factory Builder interface.
	 */
	public ChannelFactoryBuilder connectWith(){
		
		return new ChannelFactoryBuilder(this.parent, this);
	}

    /**
     * Select and configure the routing cache mechanism
     * @return Routing Cache Builder interface
     */
    public RoutingCacheBuilder cacheRoutingWith() {

        return new RoutingCacheBuilder(this.parent, this);
    }

    public void createCommandedCache(long expiryTimeInSeconds) throws BuilderException {

        if ( (null == this.channelFactory) || (null == this.topologyService) ) {

            throw new BuilderException("Cannot configure a commanded cache until a topology service and channel factory have been configured.");
        }

        ICommandReceiver commandChannel = new DefaultCommandReceiver(
                new RabbitEnvelopeReceiver(
                        topologyService,
                        channelFactory),
                null);

        this.routingInfoCache = new CommandableCache(commandChannel, expiryTimeInSeconds);
    }

	public void buildTransportProvider(){

		assert this.topologyService != null;
        assert this.channelFactory != null;
        assert this.routingInfoCache != null;

		this.setTransportProvider(
			new RabbitTransportProvider(this.topologyService, this.channelFactory, this.routingInfoCache));
	}

	/**
	 * And go back to the primary fluent interface.
	 * @return original interface.
	 */
	@Override
	public BusBuilder and() {
		
		return super.and();
	}
}