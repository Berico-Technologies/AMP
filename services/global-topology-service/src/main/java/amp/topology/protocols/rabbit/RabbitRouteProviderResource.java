package amp.topology.protocols.rabbit;

import amp.rabbit.topology.RoutingInfo;
import amp.topology.global.TopicConfiguration;
import amp.topology.global.TopicRegistry;
import amp.topology.anubis.Actor;
import amp.topology.global.filtering.RouteFilterResults;
import amp.topology.protocols.common.Versioned;
import amp.topology.protocols.rabbit.requirements.RabbitRouteRequirements;
import com.yammer.dropwizard.auth.Auth;
import com.yammer.metrics.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * This is the endpoint for resolving routes for the RabbitTransportProvider v3.3.0.
 *
 * @author Richard Clayton (Berico Technologies)
 */
@Path("/routes/rabbit")
public class RabbitRouteProviderResource {

    @Autowired
    private TopicRegistry topicRegistry;

    //@Autowired
    //private AmqpTopologyAdaptor topologyAdaptor;



    @POST
    @Path("/3.3.0")
    @Timed
    public RoutingInfo getRoutingInfo(
            @Auth UserDetails clientMakingRequest,
            @Versioned("3.3.0") RabbitRouteRequirements routeRequirements)
            throws Exception {

        // TODO: Actor is what we should get back from Anubis-based Spring Security plugin.
        Actor client = (Actor)clientMakingRequest;

        routeRequirements.setActor(client);

        // Retrieve the target route.
        TopicConfiguration topicConf = topicRegistry.get(routeRequirements.getTopic());

        // Get the applicable topology constructs (PGroups, CGroups, Connectors)
        RouteFilterResults routeResults = topicConf.filter(routeRequirements);

        // Adapt the topology constructs into AMQP Routing Info
        //return topologyAdaptor.adapt(routeResults);
        return null;
    }
}
