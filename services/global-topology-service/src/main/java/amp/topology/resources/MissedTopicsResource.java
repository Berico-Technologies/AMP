package amp.topology.resources;

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.util.JSONPObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import amp.topology.core.repo.listeners.NoRouteInfoForTopicListener;
import amp.topology.core.repo.listeners.TopicMiss;

import com.yammer.metrics.annotation.Timed;

@Path("/missed")
@Produces(MediaType.APPLICATION_JSON)
public class MissedTopicsResource {

	private static final Logger logger = LoggerFactory.getLogger(MissedTopicsResource.class);
	
	NoRouteInfoForTopicListener noRouteForTopicListener;
	
	public MissedTopicsResource(
		NoRouteInfoForTopicListener noRouteForTopicListener) {
		
		this.noRouteForTopicListener = noRouteForTopicListener;
	}
	
	@GET
	@Timed
	public Object getMissedTopics(@QueryParam("callback") String callback){
		
		logger.info("Getting missed topics.");
		
		Collection<TopicMiss> topicMisses = this.noRouteForTopicListener.getTopicMisses();

        if(callback != null && callback.length()>0){
            return new JSONPObject(callback, topicMisses);
        }
        return topicMisses;
	}
	
}
