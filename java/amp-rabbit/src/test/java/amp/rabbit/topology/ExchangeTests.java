package amp.rabbit.topology;

import java.util.HashMap;

import org.junit.*;

import static org.junit.Assert.*;

/** 
*  These tests assert that equality and hashcode generation use value semantics
*  not just in-so-far as properties have the same reference values but that 
* properties themselves are compared using a value semantic.  
*/
public class ExchangeTests extends AmqpBaseModelTests{

	protected Exchange _typeNotEquivelentModel = new Exchange();
	
	@Override
	public void setup(){
		
        _referenceModel = NewExchange("topic", "arg1");
        _equivelentModel = NewExchange("topic", "arg1");
        _notEquivelentModel = NewExchange("topic", "arg2");
        _typeNotEquivelentModel = NewExchange("direct", "arg1");

	}
	
    private Exchange NewExchange(String exchangeType, String argValue)
    {
    	Exchange exchange = new Exchange();
        exchange.arguments = new HashMap<String,Object>();
        exchange.arguments.put("1", argValue);
        exchange.setExchangeType(exchangeType);
        return exchange;
    }
    
	@Test
	public void Equality_should_consider_exchange_type(){
		assertFalse(_referenceModel.equals(_typeNotEquivelentModel));
	}

	@Test
	public void Hashcodes_should_differ_if_exchange_type_differ(){
		assertNotEquals(_referenceModel.hashCode() , _typeNotEquivelentModel.hashCode());
	}
}
