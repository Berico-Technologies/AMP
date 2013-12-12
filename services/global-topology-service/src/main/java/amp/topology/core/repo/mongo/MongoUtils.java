package amp.topology.core.repo.mongo;

import java.util.Collection;
import java.util.HashSet;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;

public class MongoUtils {

	public static Collection<String> unwindArrayToUniqueElement(
		DB database, String collection, String field){
		
		HashSet<String> results = new HashSet<String>();
		
		BasicDBObject params = new BasicDBObject(field, 1);
		params.put("_id", 0);
		BasicDBObject project = new BasicDBObject("$project", params);
		BasicDBObject unwind  = new BasicDBObject("$unwind", "$" + field);
		
		AggregationOutput output = database.getCollection(collection).aggregate(project, unwind); 
		
		for (DBObject result : output.results()){
			
			results.add(result.get(field).toString());
		}
		
		return results;
	}
	
}
