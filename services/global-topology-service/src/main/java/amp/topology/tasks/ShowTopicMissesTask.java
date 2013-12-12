package amp.topology.tasks;

import java.io.PrintWriter;
import java.util.Collection;

import amp.topology.core.repo.listeners.NoRouteInfoForTopicListener;
import amp.topology.core.repo.listeners.TopicMiss;

import com.google.common.collect.ImmutableMultimap;
import com.yammer.dropwizard.tasks.Task;

/**
 * Retrieves verbose information about all the Topics that do not
 * currently have routes in the GTS, as well as, the clients
 * using those topics and the last time they requested the topic.
 * 
 * @author Richard Clayton (Berico Technologies)
 */
public class ShowTopicMissesTask extends Task {

	NoRouteInfoForTopicListener noRouteForTopicListener;
	
	public ShowTopicMissesTask(
		NoRouteInfoForTopicListener noRouteForTopicListener) {
		
		super("show-topic-misses");
		
		this.noRouteForTopicListener = noRouteForTopicListener;
	}

	@Override
	public void execute(ImmutableMultimap<String, String> params, PrintWriter writer) 
			throws Exception {
		
		Collection<TopicMiss> topicMisses = this.noRouteForTopicListener.getTopicMisses();
		
		for (TopicMiss topicMiss : topicMisses){
			
			writer.print(topicMiss);
		}
		
		if (topicMisses.size() == 0){
			
			writer.print("No Topic Misses");
		}
	}

}
