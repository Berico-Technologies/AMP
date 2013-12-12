package amp.topology.resources;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/missed")
@Produces(MediaType.APPLICATION_JSON)
public class MissedTopicsResource {

	/*
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
	*/
}
