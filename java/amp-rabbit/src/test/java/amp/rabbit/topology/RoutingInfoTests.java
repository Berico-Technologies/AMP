package amp.rabbit.topology;

import org.junit.*;

import static org.junit.Assert.*;

/** 
*  These tests assert that equality and hashcode generation use value semantics
*  not just in-so-far as properties have the same reference values but that 
* properties themselves are compared using a value semantic.  
*/
public class RoutingInfoTests {

	protected RoutingInfo _referenceRoute = new RoutingInfo();
	protected RoutingInfo _equivelentRoute = new RoutingInfo();
	protected RoutingInfo _producingRoutesNotEquivelentRoute = new RoutingInfo();
	protected RoutingInfo _consumingRoutesNotEquivelent = new RoutingInfo();
	
	@Before
	public void setup(){
		
        _referenceRoute = newRoutingInfo("prodRoute1", "conRoute1");
        _equivelentRoute = newRoutingInfo("prodRoute1", "conRoute1");
        _producingRoutesNotEquivelentRoute = newRoutingInfo("prodRoute2", "conRoute1");
        _consumingRoutesNotEquivelent = newRoutingInfo("prodRoute1", "conRoute2");
	}

    private RoutingInfo newRoutingInfo(String producingRouteKey, String consumingRouteKey) {
        RoutingInfo routing = new RoutingInfo();
        
        routing.producingRoutes.add(new ProducingRoute());
		ProducingRoute route = routing.producingRoutes.get(0);
		route.brokers.add(new Broker("host", 0));
		route.exchange = new Exchange("exchange", "direct", true, true, true, null);
		route.routingKeys.add(producingRouteKey);

		routing.consumingRoutes.add(new ConsumingRoute());
		ConsumingRoute consumingRoute = routing.consumingRoutes.get(0);
		consumingRoute.brokers.add(new Broker("host", 0));
		consumingRoute.exchange = new Exchange("exchange", "direct", true, true, true, null);
		consumingRoute.queue = new Queue("queue", true, true, true, true, null);
		consumingRoute.routingKeys.add(consumingRouteKey);
		
		return routing;
   }

	@Test
	public void Equals_should_return_true_for_equivelent_routes(){
		assertTrue(_referenceRoute.equals(_equivelentRoute));
	}
	
	@Test
	public void Equals_should_return_false_if_consuming_routes_differ(){
		assertFalse(_referenceRoute.equals(_consumingRoutesNotEquivelent));
	}
	
	@Test
	public void Equals_should_return_false_if_producing_routes_differ(){
		assertFalse(_referenceRoute.equals(_producingRoutesNotEquivelentRoute));
	}
	
	@Test
	public void Hashcodes_should_not_differ_for_equivelent_routes(){
		assertEquals(_referenceRoute.hashCode() , _equivelentRoute.hashCode());
	}

	@Test
	public void Hashcodes_should_differ_if_consuming_routes_differ(){
		assertNotEquals(_referenceRoute.hashCode() , _consumingRoutesNotEquivelent.hashCode());
	}

	@Test
	public void Hashcodes_should_differ_if_producing_routes_differ(){
		assertNotEquals(_referenceRoute.hashCode() , _producingRoutesNotEquivelentRoute.hashCode());
	}
}
