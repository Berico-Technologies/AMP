package amp.topology.protocols.rabbit;

import amp.rabbit.topology.RoutingInfo;
import amp.topology.anubis.Actor;
import amp.topology.anubis.SpringActor;
import amp.topology.global.TopicRegistry;
import amp.topology.global.filtering.RouteFilterResults;
import amp.topology.resources.common.Versioned;
import amp.topology.protocols.rabbit.requirements.RabbitRouteRequirements;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.yammer.dropwizard.auth.Auth;
import com.yammer.metrics.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * This is the endpoint for resolving routes for the RabbitTransportProvider v3.3.0.
 *
 * @author Richard Clayton (Berico Technologies)
 */
@Path("/protocols/rabbit")
@Component
@Api(
        value = "service/protocols/rabbit",
        description = "Routing Information services for the Rabbit (AMQP) Protocol."
)
@Produces({ "application/json;qs=1", "application/xml;qs=.5" })
public class RabbitRouteProviderResource {

    @Autowired
    private TopicRegistry topicRegistry;

    @POST
    @Path("/3.3.0")
    @Timed
    @ApiOperation(
            value = "Get Routing Info v3.3.0",
            notes = "Get routing info based on the requirements supplied in the request.",
            response = RoutingInfo.class,
            tags = "v3.3.0"
    )
    public RoutingInfo getRoutingInfo(
            @Auth @ApiParam(access = "no") UserDetails clientMakingRequest,
            @Versioned("3.3.0") RabbitRouteRequirements routeRequirements)
            throws Exception {

        // TODO: Actor is what we should get back from Anubis-based Spring Security plugin.
        Actor client = new SpringActor(clientMakingRequest);

        routeRequirements.setActor(client);

        // Retrieve the target route.
        amp.topology.global.Topic topicConf = topicRegistry.get(routeRequirements.getTopic());

        // Get the applicable topology constructs (PGroups, CGroups, Connectors)
        RouteFilterResults routeResults = topicConf.filter(routeRequirements);

        // Convert the results into routing info.
        return RouteFilterResultsAdaptor.convert(routeResults, routeRequirements);
    }

    /**
     * Set the BasicTopic Registry (used for Unit Testing purposes).
     *
     * @param topicRegistry BasicTopic Registry to set.
     */
    public void setTopicRegistry(TopicRegistry topicRegistry) {

        this.topicRegistry = topicRegistry;
    }
}
