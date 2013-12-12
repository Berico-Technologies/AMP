package amp.rabbit.topology;

import org.junit.*;

import static org.junit.Assert.*;

/** 
*  These tests assert that equality and hashcode generation use value semantics
*  not just in-so-far as properties have the same reference values but that 
* properties themselves are compared using a value semantic.  
*/
public class BaseRouteTests {

	protected BaseRoute _referenceModel;
	protected BaseRoute _equivelentModel;
	protected BaseRoute _brokerNotEquivelentModel;
	protected BaseRoute _exchangeNotEquivelentModel;
	protected BaseRoute _routeNotEquivelentModel;
	
	@Before
	public void setup(){
		
        _referenceModel = new TestRoute("broker1", "exchange1", "route1");
        _equivelentModel = new TestRoute("broker1", "exchange1", "route1");
        _brokerNotEquivelentModel = new TestRoute("broker2", "exchange1", "route1");
        _exchangeNotEquivelentModel = new TestRoute("broker1", "exchange2", "route1");
        _routeNotEquivelentModel = new TestRoute("broker1", "exchange1", "route2");
	}

	@Test
	public void Equals_should_return_true_for_equivelent_routes(){
		assertTrue(_referenceModel.equals(_equivelentModel));
	}
	
	@Test
	public void Equals_should_return_false_if_broker_values_differ(){
		assertFalse(_referenceModel.equals(_brokerNotEquivelentModel));
	}
	
	@Test
	public void Equals_should_return_false_if_exchange_values_differ(){
		assertFalse(_referenceModel.equals(_exchangeNotEquivelentModel));
	}
	
	@Test
	public void Equals_should_return_false_if_route_values_differ(){
		assertFalse(_referenceModel.equals(_routeNotEquivelentModel));
	}
	
	@Test
	public void Hashcodes_should_not_differ_for_equivelent_routes(){
		assertEquals(_referenceModel.hashCode() , _equivelentModel.hashCode());
	}

	@Test
	public void Hashcodes_should_differ_if_broker_entries_differ(){
		assertNotEquals(_referenceModel.hashCode() , _brokerNotEquivelentModel.hashCode());
	}
	
	@Test
	public void Hashcodes_should_differ_if_exchanges_differ(){
		assertNotEquals(_referenceModel.hashCode() , _exchangeNotEquivelentModel.hashCode());
	}
	
	@Test
	public void Hashcodes_should_differ_if_routingKey_entries_differ(){
		assertNotEquals(_referenceModel.hashCode() , _routeNotEquivelentModel.hashCode());
	}
	
	private static class TestRoute extends BaseRoute {
		 public TestRoute(String host, String exchange, String routeKey) {
				this.brokers.add(new Broker(host, 0, "basic"));
				this.exchange = new Exchange(exchange, "direct", true, true, true, null);
				this.routingKeys.add(routeKey);
		 }
	}
}
