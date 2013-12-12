package amp.topology.core.repo.mongo;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;

@Ignore("Requires an active MongoDB Connection with entries in the Repository")
public class MongoUtilsTest {

	FileSystemXmlApplicationContext context;
	MongoTemplate mongoTemplate;
	
	@Before
	public void setup(){
		
		context = new FileSystemXmlApplicationContext(
				"configuration/mongoContext.xml", "configuration/applicationContext.xml");
			
		mongoTemplate = context.getBean(MongoTemplate.class);
	}
	
	@After
	public void tearDown(){
		
		context.close();
	}
	
	@Test
	public void mongodb_aggregation_query_returns_results_for_clients() {
		
		Collection<String> results = 
			MongoUtils.unwindArrayToUniqueElement(
				mongoTemplate.getDb(), "extendedRouteInfo", "clients");
		
		assertTrue(results.size() > 0);
	}

	@Test
	public void mongodb_aggregation_query_returns_results_for_topics() {
		
		Collection<String> results = 
			MongoUtils.unwindArrayToUniqueElement(
				mongoTemplate.getDb(), "extendedRouteInfo", "topics");
		
		assertTrue(results.size() > 0);
	}
}
