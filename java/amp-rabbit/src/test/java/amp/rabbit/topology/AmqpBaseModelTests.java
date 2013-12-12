package amp.rabbit.topology;

import java.util.HashMap;

import org.junit.*;

import static org.junit.Assert.*;

/** 
*  These tests assert that equality and hashcode generation use value semantics
*  not just in-so-far as properties have the same reference values but that 
* properties themselves are compared using a value semantic.  
*/
public class AmqpBaseModelTests {

	protected AmqpBaseModel _referenceModel;
	protected AmqpBaseModel _equivelentModel;
	protected AmqpBaseModel _notEquivelentModel;
	
	@Before
	public void setup(){
		
		_referenceModel = new TestModel();
		_referenceModel.arguments.put("1", "one");

		_equivelentModel = new TestModel();
		_equivelentModel.arguments.put("1", "one");

		_notEquivelentModel = new TestModel();
		_notEquivelentModel.arguments.put("1", "uno");

	}

	@Test
	public void Equality_should_consider_argument_contents(){
		assertTrue(_referenceModel.equals(_equivelentModel));
	}
	
	@Test
	public void Equality_should_consider_argument_contents_not_equals(){
		assertFalse(_referenceModel.equals(_notEquivelentModel));
	}
	
	@Test
	public void Hashcodes_should_not_differ_if_noting_differs(){
		assertEquals(_referenceModel.hashCode() , _equivelentModel.hashCode());
	}

	@Test
	public void Hashcodes_should_differ_if_argument_contents_differ(){
		assertNotEquals(_referenceModel.hashCode() , _notEquivelentModel.hashCode());
	}

	private static class TestModel extends AmqpBaseModel {
        public TestModel() {
            super("test", true, true, true, new HashMap<String, Object>());
        }
	}
}
