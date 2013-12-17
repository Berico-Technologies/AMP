package amp.topology.resources;

import java.util.HashMap;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import amp.messaging.EnvelopeHeaderConstants;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;



import amp.rabbit.topology.ITopologyService;
import amp.rabbit.topology.RoutingInfo;

import com.yammer.dropwizard.auth.Auth;
import com.yammer.metrics.annotation.Timed;

@Path("/topology")
@Produces(MediaType.APPLICATION_JSON)
public class TopologyServiceResource {

	private static final Logger logger = LoggerFactory.getLogger(TopologyServiceResource.class);
	
	//TODO: Replicated Until We Find a Better place for them.
	public static final String HEADER_REQUEST_TOPO_CREATION = "amp.topology.request.createForClient";
	public static final String HEADER_PREFERRED_QUEUENAME = "amp.topology.request.prefs.queue.name";
	public static final String HEADER_PREFERRED_QUEUE_PREFIX = "amp.topology.request.prefs.queue.prefix";
	
	private ITopologyService topologyService;
	
	public TopologyServiceResource(ITopologyService topologyService){
		
		this.topologyService = topologyService;
	}

	@GET
	@Timed
	@Path("/get-routing-info/{topic}")
	public Object getRoutingInfo(
			@Auth UserDetails client, 
			@PathParam("topic") String topic,
			@DefaultValue("false") @QueryParam("c") String shouldCreate,
			@QueryParam("qn") String queueName,
			@QueryParam("qp") String queuePrefix,
            @QueryParam("callback") String callback){
		
		HashMap<String, String> routingHints = new HashMap<String, String>();
		
		logger.info("Getting Routing Info for client '{}' for topic '{}'.", client.getUsername(), topic);
		
		routingHints.put(EnvelopeHeaderConstants.MESSAGE_SENDER_IDENTITY, client.getUsername());
		
		routingHints.put(EnvelopeHeaderConstants.MESSAGE_TOPIC, topic);
		
		if (!shouldCreate.equalsIgnoreCase("false")){
			
			logger.info("Client requests routes be precreated.");
			
			routingHints.put(HEADER_REQUEST_TOPO_CREATION, "true");
			
			if (queueName != null){
				
				logger.info("Client requests queueName be '{}'", queueName);
				
				routingHints.put(HEADER_PREFERRED_QUEUENAME, queueName);
			}
			
			if (queuePrefix != null){
				
				logger.info("Client requests queue prefix of '{}'", queuePrefix);
				
				routingHints.put(HEADER_PREFERRED_QUEUE_PREFIX, queuePrefix);
			}
		}else{
            logger.info("Client requests routes NOT be precreated");
        }

        RoutingInfo routingInfo = this.topologyService.getRoutingInfo(routingHints);
        if(callback != null && callback.length()>0){
            return new JSONPObject(callback, routingInfo);
        }
		return routingInfo;
	}
	
}
