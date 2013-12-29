package amp.topology.protocols.rabbit;

import amp.rabbit.topology.RoutingInfo;
import amp.topology.anubis.Actor;
import amp.topology.anubis.SpringActor;
import amp.topology.global.TopicConfiguration;
import amp.topology.global.TopicRegistry;
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

    @POST
    @Path("/3.3.0")
    @Timed
    public RoutingInfo getRoutingInfo(
            @Auth UserDetails clientMakingRequest,
            @Versioned("3.3.0") RabbitRouteRequirements routeRequirements)
            throws Exception {

        // TODO: Actor is what we should get back from Anubis-based Spring Security plugin.
        Actor client = new SpringActor(clientMakingRequest);

        routeRequirements.setActor(client);

        // Retrieve the target route.
        TopicConfiguration topicConf = topicRegistry.get(routeRequirements.getTopic());

        // Get the applicable topology constructs (PGroups, CGroups, Connectors)
        RouteFilterResults routeResults = topicConf.filter(routeRequirements);

        // Convert the results into routing info.
        RoutingInfo routingInfo = RouteFilterResultsAdaptor.convert(routeResults, routeRequirements);

        // return the results.
        return routingInfo;
    }
}
