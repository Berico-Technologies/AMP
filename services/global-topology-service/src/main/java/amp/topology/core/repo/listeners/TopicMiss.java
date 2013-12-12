package amp.topology.core.repo.listeners;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.joda.time.DateTime;

public class TopicMiss {
	
	String topic;
	
	ConcurrentHashMap<String, Long> userToLastRequestTimeMapping = 
			new ConcurrentHashMap<String, Long>();
	
	public TopicMiss(String topic){
		
		this.topic = topic;
	}
	
	public void logMiss(String client){
		long timestamp = System.currentTimeMillis();
		
		this.userToLastRequestTimeMapping.put(client, timestamp);
	}
	
	public long getLastMiss(String client){
		
		return this.userToLastRequestTimeMapping.get(client);
	}
	
	public String getTopic(){
		
		return this.topic;
	}
	
	public Map<String, Long> getUserToLastRequest(){
		
		return Collections.unmodifiableMap(this.userToLastRequestTimeMapping);
	}
	
	public Collection<String> getClients(){
		
		return Collections.unmodifiableCollection(
			this.userToLastRequestTimeMapping.keySet());
	}
	
	@Override
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("Topic Misses for '").append(topic).append("':\n");
		
		for(Entry<String, Long> entry : userToLastRequestTimeMapping.entrySet()){
			
			sb.append("\t")
			  .append(entry.getKey())
			  .append("->")
			  .append(new DateTime(entry.getValue()))
			  .append("\n");
		}
		
		return sb.toString();
	}
}