package amp.topology.resources;

import amp.topology.factory.ConnectorFactory;
import amp.topology.factory.ConnectorSpecification;
import amp.topology.factory.Modifications;
import amp.topology.global.Connector;
import amp.topology.global.TopicConfiguration;
import amp.topology.global.TopicRegistry;
import amp.topology.global.TopologyGroup;
import amp.topology.resources.common.Versioned;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.annotation.Timed;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;

/**
 * Endpoint for managing Groups in the Global Topology.
 *
 * @author Richard Clayton (Berico Technologies)
 */
@Path("topology/connectors")
@Api(
        value = "service/topology/connectors",
        description = "Manage connectors in the Global Topology."
)
@Produces({ "application/json;qs=1", "application/xml;qs=0.5" })
public class ConnectorResource {

    ConnectorFactory connectorFactory;

    TopicRegistry topicRegistry;

    public void setConnectorFactory(ConnectorFactory connectorFactory) {

        this.connectorFactory = connectorFactory;
    }

    public void setTopicRegistry(TopicRegistry topicRegistry) {

        this.topicRegistry = topicRegistry;
    }

    @PUT
    @ApiOperation(
            value = "Adds a Connector to a Topic in the Global Topology.",
            notes = "Adds a Connector the the supplied specifications to a Topic in the Global Topology.",
            response = TopologyGroup.class,
            authorizations = "gts-connectors-add"
    )
    @Timed
    public Connector<?, ?> add(@Versioned ConnectorSpecification specification) throws Exception {

        return connectorFactory.create(specification);
    }

    @DELETE
    @Path("/{topicId}/{connectorId}")
    @ApiOperation(
            value = "Remove a Connector from the Global Topology.",
            notes = "Removes the connector with the given ID from the Global Topology, cleaning up resources as needed.",
            authorizations = "gts-connectors-remove"
    )
    @ApiResponses({
            @ApiResponse(code=404, message="No topic with specified id."),
            @ApiResponse(code=404, message="No connector with specified id.")
    })
    @Timed
    public Response remove(@PathParam("topicId") String topicId, @PathParam("connectorId") String connectorId) throws Exception {

        this.topicRegistry.get(topicId).removeConnector(connectorId);

        return Response.ok().build();
    }

    @GET
    @Path("/{topicId}")
    @ApiOperation(
            value = "Retrieve all connectors from the Global Topology.",
            notes = "Retrieves all Connectors for a Topic with the given ID in the Global Topology.",
            authorizations = "gts-connectors-list",
            response = ConnectorsCollection.class
    )
    @ApiResponses({
            @ApiResponse(code=404, message="No topic with specified id.")
    })
    @Timed
    @Metered(name="connectors-list")
    public ConnectorsCollection list(@PathParam("topicId") String topicId) throws Exception {

        TopicConfiguration topic = this.topicRegistry.get(topicId);

        return new ConnectorsCollection(topic);
    }

    @GET
    @Path("/{topicId}/{connectorId}")
    @ApiOperation(
            value = "Retrieve a specific connector from the Global Topology.",
            notes = "Retrieves a Connector for a Topic with the given ID in the Global Topology.",
            authorizations = "gts-groups-list",
            response = Connector.class
    )
    @ApiResponses({
            @ApiResponse(code=404, message="No topic with specified id."),
            @ApiResponse(code=404, message="No connector with specified id.")
    })
    @Timed
    @Metered(name="connectors-get")
    public Connector<?,?> get(@PathParam("topicId") String topicId, @PathParam("connectorId") String connectorId) throws Exception {

        return this.topicRegistry.get(topicId).getConnector(connectorId);
    }

    @POST
    @ApiOperation(
            value = "Modify a connector in the Global Topology.",
            notes = "Modifies an existing connector with the information from the supplied ConnectorSpecification.",
            authorizations = "gts-connectors-modify",
            response = Modifications.class
    )
    @ApiResponses({
            @ApiResponse(code=404, message="No topic with specified id."),
            @ApiResponse(code=404, message="No connector with specified id.")
    })
    @Timed
    public Modifications modify(@Versioned ConnectorSpecification specification) throws Exception {

        return this.connectorFactory.modify(specification);
    }

    /**
     * Wraps a Connector Collection in a transportable object.
     */
    @XmlRootElement
    public static class ConnectorsCollection  {

        private final Collection<Connector<?,?>> connectors;

        public ConnectorsCollection(TopicConfiguration topicConfiguration) {

            this.connectors = topicConfiguration.getConnectors();
        }

        public Collection<Connector<?, ?>> getConnectors() {

            return connectors;
        }
    }
}
