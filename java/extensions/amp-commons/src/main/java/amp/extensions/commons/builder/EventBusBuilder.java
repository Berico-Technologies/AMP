package amp.extensions.commons.builder;

import java.util.HashMap;
import java.util.LinkedList;

import cmf.bus.IEnvelopeBus;
import cmf.eventing.IEventBus;
import cmf.eventing.patterns.rpc.IRpcEventBus;

import amp.bus.security.InMemoryUserInfoRepository;
import amp.eventing.DefaultEventBus;
import amp.eventing.DefaultRpcBus;
import amp.messaging.IMessageProcessor;
import amp.messaging.OutboundHeadersProcessor;
import amp.eventing.RpcFilter;
import amp.messaging.JsonSerializationProcessor;;

/**
 * Fluent for constructing an EventBus.
 * 
 * This implementation includes a set of default processors
 * that you will most likely need to implement an EventBus:
 * RpcFilter, Serializer Processor (JSON), and HeadersProcessor.
 * 
 * @author Richard Clayton (Berico Technologies)
 */
public class EventBusBuilder extends FluentExtension {

	LinkedList<IMessageProcessor> inbound = new LinkedList<IMessageProcessor>();
	LinkedList<IMessageProcessor> outbound = new LinkedList<IMessageProcessor>();
	boolean usingDefaults = true;
	
	/**
	 * Instantiate the Builder with reference to the parent fluent.
	 * @param parent BusBuilder fluent.
	 */
	public EventBusBuilder(BusBuilder parent) {
		
		super(parent);
	
		setDefaults();
	}
	
	/**
	 * Set the default processors on the bus.
	 */
	void setDefaults(){
		
		// Allows respondTo, etc.
		RpcFilter rpcFilter = new RpcFilter();
		
		// Got to marshall objects (Default is JSON)
		JsonSerializationProcessor jsonEventSerializer = 
				new JsonSerializationProcessor();

		InMemoryUserInfoRepository userInfoRepository = 
				new InMemoryUserInfoRepository(new HashMap<String, String>());

		// Manipulates the headers for use by the Event Bus.
		OutboundHeadersProcessor headersProcessor = new OutboundHeadersProcessor();
		headersProcessor.setUserInfoRepo(userInfoRepository);
		// These are order dependent.
		this.inbound.add(rpcFilter);
		this.inbound.add(jsonEventSerializer);
		
		// These are order dependent.
		this.outbound.add(jsonEventSerializer);
		this.outbound.add(headersProcessor);
		this.outbound.add(rpcFilter);
	}
	
	/**
	 * Add an inbound processor.  When you do this, you will
	 * clear the default processors and void your Warranty.
	 * @param processor Processor to add.
	 * @return This.
	 */
	public EventBusBuilder addInbound(IMessageProcessor processor){
		
		voidWarranty();
		
		this.inbound.add(processor);
		
		return this;
	}
	
	/**
	 * Add an outbound processor. When you do this, you will
	 * clear the default processors and void your Warranty.
	 * @param processor Processor to add.
	 * @return This.
	 */
	public EventBusBuilder addOutbound(IMessageProcessor processor){
		
		voidWarranty();
		
		this.outbound.add(processor);
		
		return this;
	}
	
	/**
	 * Removes defaults.
	 */
	void voidWarranty(){
		
		if (this.usingDefaults){
			
			this.inbound.clear();
			this.outbound.clear();
			
			this.usingDefaults = false;
		}
	}
	
	/**
	 * Get an instance of the EventBus.
	 * @return a configured Event Bus.
	 */
	public IEventBus getInstance(){
		
		IEnvelopeBus envelopeBus = this.parent.envelopeBus().getInstance();
		
		return new DefaultEventBus(envelopeBus, inbound, outbound);
	}
	
	/**
	 * Get an instance of the RpcEventBus.
	 * @return a configured RPC-capable Event Bus.
	 */
	public IRpcEventBus getRpcBusInstance(){
		
		IEnvelopeBus envelopeBus = this.parent.envelopeBus().getInstance();
		
		return new DefaultRpcBus(envelopeBus, inbound, outbound);
	}
}
