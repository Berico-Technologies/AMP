package amp.rabbit.topology;

import java.util.HashMap;
import java.util.Map;

public class TopologyUtils {

	public static Exchange clone(Exchange target){
		
		return Exchange.builder()
				.name(target.getName())
				.arguments(clone(target.getArguments()))
				.declare(target.shouldDeclare())
				.isAutoDelete(target.isAutoDelete())
				.isDurable(target.isDurable())
				.type(target.getExchangeType())
				.build();
	}
	
	public static Queue clone(Queue target){
		
		return Queue.builder()
				.name(target.getName())
				.arguments(clone(target.getArguments()))
				.declare(target.shouldDeclare())
				.isAutoDelete(target.isAutoDelete())
				.isDurable(target.isDurable())
				.isExclusive(target.isExclusive())
				.build();
	}

	public static Map<String, Object> clone(Map<String, Object> target){
		
		HashMap<String, Object> clone = new HashMap<String, Object>();
		
		if (target != null){
		
			clone.putAll(target);
		}
		
		return clone;
	}
}