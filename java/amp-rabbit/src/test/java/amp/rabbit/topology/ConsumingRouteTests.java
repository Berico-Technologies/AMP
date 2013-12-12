package amp.rabbit.topology;

import static org.junit.Assert.*;

import org.junit.*;

/** 
*  These tests assert that equality and hashcode generation use value semantics
*  not just in-so-far as properties have the same reference values but that 
* properties themselves are compared using a value semantic.  
*/
public class ConsumingRouteTests extends BaseRouteTests {
	
	protected ConsumingRoute _queueNotEquivelentModel;

	@Override 
	public void setup() {
		
        _referenceModel = newConsummingRoute("broker1", "exchange1", "queue1", "route1");
        _equivelentModel = newConsummingRoute("broker1", "exchange1", "queue1", "route1");
        _brokerNotEquivelentModel = newConsummingRoute("broker2", "exchange1", "queue1", "route1");
        _exchangeNotEquivelentModel = newConsummingRoute("broker1", "exchange2", "queue1", "route1");
        _routeNotEquivelentModel = newConsummingRoute("broker1", "exchange1", "queue1", "route2");
        _queueNotEquivelentModel = newConsummingRoute("broker1", "exchange1", "queue2", "route1");
	}
	
    private ConsumingRoute newConsummingRoute(String host, String exchange, String queue, String routeKey){
    	ConsumingRoute route = new ConsumingRoute();
    	
    	route.brokers.add(new Broker(host, 0, "basic"));
    	route.exchange = new Exchange(exchange, "direct", true, true, true, null);
    	route.queue = new Queue(queue, true, true, true, true, null);
    	route.routingKeys.add(routeKey);
  	
        return route;
    }
    
	@Test
	public void Equals_should_return_false_if_queue_values_differ(){
		assertFalse(_referenceModel.equals(_queueNotEquivelentModel));
	}
	
	@Test
	public void Hashcodes_should_differ_if_queue_entries_differ(){
		assertNotEquals(_referenceModel.hashCode() , _queueNotEquivelentModel.hashCode());
	}
}
