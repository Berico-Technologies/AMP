package amp.topology.health;

import java.util.Collection;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import amp.topology.core.repo.listeners.NoRouteInfoForTopicListener;
import amp.topology.core.repo.listeners.TopicMiss;

import com.yammer.metrics.core.HealthCheck;

/**
 * Displays the number of missed topics and the breadth of clients experiencing
 * misses.  This is an overall indication of how many clients are NOT USING
 * the GTS for their topology.  
 * 
 * @author Richard Clayton (Berico Technologies)
 */
public class TopicMissHealthCheck extends HealthCheck {

	private static Logger logger = LoggerFactory.getLogger(TopicMissHealthCheck.class);
	
	public static int MISSED_TOPICS_THRESHOLD = 20;
	public static int MISSED_CLIENTS_THRESHOLD = 20;
	public static String MESSAGE_EXPRESSION = "Missed Topics [%s], Missed Clients [%s]";
	
	NoRouteInfoForTopicListener noRouteForTopicListener;
	
	public TopicMissHealthCheck(NoRouteInfoForTopicListener noRouteForTopicListener) {
		super("topic misses");
		
		this.noRouteForTopicListener = noRouteForTopicListener;
	}

	@Override
	protected Result check() throws Exception {
		
		logger.debug("Checking the status of the TopicMisses");
		
		Collection<TopicMiss> topicMisses = this.noRouteForTopicListener.getTopicMisses();
		
		int numberOfMissedTopics = topicMisses.size();
		
		TreeSet<String> clients = new TreeSet<String>();
		
		for (TopicMiss topicMiss : topicMisses){
			
			clients.addAll(topicMiss.getClients());
		}
		
		int numberOfUniqueClientsMissingTopics = clients.size();
		
		String message = String.format(MESSAGE_EXPRESSION, 
			numberOfMissedTopics, numberOfUniqueClientsMissingTopics);
		
		if (numberOfMissedTopics > MISSED_TOPICS_THRESHOLD 
			|| numberOfUniqueClientsMissingTopics > MISSED_CLIENTS_THRESHOLD){
			
			return Result.unhealthy(message);
		}
		else {
			
			return Result.healthy(message);
		}
	}

}
