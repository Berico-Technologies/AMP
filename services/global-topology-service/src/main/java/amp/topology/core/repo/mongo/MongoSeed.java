package amp.topology.core.repo.mongo;

import java.util.Arrays;

import org.springframework.data.mongodb.core.MongoTemplate;

import amp.topology.core.ExtendedExchange;
import amp.topology.core.ExtendedRouteInfo;
//import static org.springframework.data.mongodb.core.query.Criteria.where;
//import static org.springframework.data.mongodb.core.query.Query.query;


public class MongoSeed {
	
	private MongoTemplate mongoTemplate;

	public MongoSeed(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}
	
	public void seed(){
		
		mongoTemplate.dropCollection(ExtendedExchange.class);
		mongoTemplate.dropCollection(ExtendedRouteInfo.class);
		
		ExtendedExchange securityExchange = 
			new ExtendedExchange("Security Exchange", "cmf.security", "localhost", "/", 
				5672, "cmf.security", "security-service", "topic", false, false, null);
		
		mongoTemplate.save(securityExchange);
		
		ExtendedExchange applicationExchange = 
				new ExtendedExchange("Applications Exchange", "cmf.apps", "localhost", "/", 
					5672, "cmf.apps", "~UNIQUE~", "topic", false, false, null);
		
		mongoTemplate.save(applicationExchange);
		
		ExtendedRouteInfo route = new ExtendedRouteInfo(
			"Security Routes", 
			Arrays.asList("app01", "app02", "Topology Client"), 
			Arrays.asList("AccessEvent", "cmf.security.AccessEvent"), 
			securityExchange.getId(), 
			securityExchange.getId());
		
		mongoTemplate.save(route);
	}

}
