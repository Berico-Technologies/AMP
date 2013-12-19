package amp.topology.protocols.webstomp;

import amp.rabbit.topology.RoutingInfo;
import amp.topology.global.TopicConfiguration;
import amp.topology.global.TopicRegistry;
import amp.topology.anubis.Actor;
import amp.topology.global.filtering.RouteFilterResults;
import amp.topology.protocols.common.Versioned;
import amp.topology.protocols.webstomp.requirements.WebStompRouteRequirements;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.yammer.dropwizard.auth.Auth;
import com.yammer.metrics.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import javax.ws.rs.*;

/**
 * This is the endpoint for resolving routes for the WebStompTransportProvider (AMPjs) v3.3.0.
 *
 * @author Richard Clayton (Berico Technologies)
 */
@Path("/routes/webstomp")
public class WebStompRouteProviderResource {

    @Autowired
    private TopicRegistry topicRegistry;

    //@Autowired
    //private WebStompTopologyAdaptor topologyAdaptor;


    @GET
    @Path("/3.3.0/{topic}")
    @Timed
    public Object getRoutingInfo(
            @Auth UserDetails clientMakingRequest,
            @PathParam("topic") String topic,
            @DefaultValue("false") @QueryParam("c") String shouldCreate,
            @QueryParam("qn") String queueName,
            @QueryParam("qp") String queuePrefix,
            @QueryParam("callback") String callback,
            @Versioned("3.3.0") WebStompRouteRequirements routeRequirements)
            throws Exception {

        // TODO: Actor is what we should get back from Anubis-based Spring Security plugin.
        Actor client = (Actor)clientMakingRequest;

        routeRequirements.setActor(client);

        // Retrieve the target route.
        TopicConfiguration topicConf = topicRegistry.get(routeRequirements.getTopic());

        // Get the applicable topology constructs (PGroups, CGroups, Connectors)
        RouteFilterResults routeResults = topicConf.filter(routeRequirements);

        // Adapt the topology constructs into AMQP Routing Info
        //RoutingInfo routingInfo = topologyAdaptor.adapt(routeResults);
        RoutingInfo routingInfo = null;

        if (callback != null && callback.length() > 0){

            return new JSONPObject(callback, routingInfo);
        }

        return routingInfo;
    }
}
