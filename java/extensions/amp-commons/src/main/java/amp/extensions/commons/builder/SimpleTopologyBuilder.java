package amp.extensions.commons.builder;

import java.util.UUID;

import amp.rabbit.topology.Broker;
import amp.rabbit.topology.Exchange;
import amp.rabbit.topology.SimpleTopologyService;

/**
 * Build up a Simple Topology Service.
 * 
 * @author Richard Clayton (Berico Technologies)
 */
public class SimpleTopologyBuilder extends FluentExtension {

	String hostname;
	String vhost = "/";
	int port = 5672;
	String defaultExchange = "cmf.simple";
	String clientName = UUID.randomUUID().toString();
    BusBuilder busBuilder;
	TransportBuilder transportBuilder;
	
	/**
	 * Initialize the builder with a reference to the parent fluent interface.
	 * @param parent Parent builder.
	 * @param transportBuilder Builder to set the topology service.
	 */
	public SimpleTopologyBuilder(BusBuilder parent, TransportBuilder transportBuilder) {
		
		super(parent);

		this.busBuilder = parent;
		this.transportBuilder = transportBuilder;
	}
	
	/**
	 * Using the following broker.
	 * @param hostname Hostname of the broker.
	 * @return This.
	 */
	public SimpleTopologyBuilder broker(String hostname){
		
		this.hostname = hostname;
		
		return this;
	}
	
	/**
	 * Using the following broker.
	 * @param hostname Hostname of the broker.
	 * @param port Port of the broker.
	 * @return This.
	 */
	public SimpleTopologyBuilder broker(String hostname, int port){
			
			this.hostname = hostname;
			this.port = port;
			
			return this;
		}
	
	/**
	 * Using the following broker.
	 * @param hostname Hostname of the broker.
	 * @param port Port of the broker.
	 * @param vhost Virtual Host on the broker.
	 * @return This.
	 */
	public SimpleTopologyBuilder broker(String hostname, int port, String vhost){
		
		this.hostname = hostname;
		this.port = port;
		this.vhost = vhost;
		
		return this;
	}
	
	/**
	 * Default exchange to send and receive messages.
	 * @param exchangeName Name of the exchange 
	 * @return This.
	 */
	public SimpleTopologyBuilder exchange(String exchangeName){
	
		this.defaultExchange = exchangeName;
		
		return this;
	}
	
	/**
	 * Using the supplied client name to identify queues being used.
	 * @param clientName Client name.
	 * @return This.
	 */
	public SimpleTopologyBuilder withClientName(String clientName){
		
		this.clientName = clientName;
		
		return this;
	}
	
	/**
	 * Build the Topology Service and return to the parent fluent.
	 * @return Parent Fluent.
	 */
	public TransportBuilder andThen(){
		
		buildTopologyService();
		
		return this.transportBuilder;
	}
	
	/**
	 * Go back to the parent interface.
	 */
	@Override
	public BusBuilder and() {
		
		buildTopologyService();
		
		return this.transportBuilder.and();
	}
	
	/**
	 * Build the topology service and set it on the transport fluent.
	 */
	void buildTopologyService(){
		
		Broker broker = new Broker(hostname,port);
		broker.setVirtualHost(vhost);
		
		SimpleTopologyService sts = 				
			new SimpleTopologyService(clientName, broker);
		
		Exchange exchange = new Exchange(this.defaultExchange,"topic",true,false,false,null);
		 
		sts.setExchangePrototype(exchange);
		
		this.transportBuilder.setTopologyService(sts);
	}
}