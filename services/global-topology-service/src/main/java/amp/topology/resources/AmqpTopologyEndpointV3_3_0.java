package amp.topology.resources;

import amp.rabbit.topology.RoutingInfo;
import amp.topology.TopologyConfiguration;
import amp.topology.global.Constants;
import amp.topology.global.TopicConfiguration;
import amp.topology.global.TopologyRegistry;
import amp.topology.global.anubis.Actor;
import amp.topology.global.filtering.RouteFilterResults;
import amp.topology.global.filtering.RouteRequirements;
import amp.topology.global.protocols.AmqpTopologyAdaptor;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.yammer.dropwizard.auth.Auth;
import com.yammer.metrics.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import sun.plugin.dom.exception.InvalidStateException;

import javax.ws.rs.*;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 * @author Richard Clayton (Berico Technologies)
 */
@Path("/routes/amqp/v3.3.0")
public class AmqpTopologyEndpointV3_3_0 {

    @Autowired
    private TopologyRegistry topologyRegistry;

    @Autowired
    private AmqpTopologyAdaptor topologyAdaptor;



    @POST
    @Timed
    public RoutingInfo getRoutingInfo(
            @Auth UserDetails clientMakingRequest,
            // We will have a custom Jersey MessageBodyReader
            // https://jersey.java.net/apidocs/latest/jersey/javax/ws/rs/ext/MessageBodyReader.html
            Map<String, String> routingInfo)
            throws Exception {

        // TODO: Actor is what we should get back from Anubis-based Spring Security plugin.
        Actor client = (Actor)clientMakingRequest;


        // Convert the request into requirements
        RouteRequirements requirements = new AmqpRouteRequirements(routingInfo, client);

        // Retrieve the target route.
        TopicConfiguration topicConf = topologyRegistry.get(requirements.getTopic());

        // Get the applicable topology constructs (PGroups, CGroups, Connectors)
        RouteFilterResults routeResults = topicConf.filter(requirements);

        // Adapt the topology constructs into AMQP Routing Info
        return topologyAdaptor.adapt(routeResults);
    }



    /**
     * Pretty simple class that converts the RouteInfo received by the client into RouteRequirements.
     */
    private static class AmqpRouteRequirements extends HashMap<String, String> implements RouteRequirements {

        private final Actor actor;

        public AmqpRouteRequirements(Map<String, String> routeInfo, Actor actor) throws IllegalStateException {

            this.actor = actor;

            if (routeInfo != null && routeInfo.size() > 0){

                this.putAll(routeInfo);
            }

            validate();
        }

        private void validate() throws IllegalStateException {

            // Check to ensure all the values are valid.
        }

        @Override
        public String getTopic() {

            return this.get(Constants.MESSAGE_TOPIC);
        }

        @Override
        public String getProtocol() {

            return Constants.PROTOCOL_AMQP;
        }

        @Override
        public String getMessagePattern() {

            return this.get(Constants.MESSAGE_PATTERN);
        }

        @Override
        public Actor getActor() {

            return this.actor;
        }

        @Override
        public RouteDirections getRouteDirection() {

            String messageDirection = this.get(Constants.MESSAGE_DIRECTION);

            return RouteDirections.valueOf(messageDirection);

        }
    }
}
