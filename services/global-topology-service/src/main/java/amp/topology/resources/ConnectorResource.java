package amp.topology.resources;

import amp.topology.factory.ConnectorFactory;
import amp.topology.factory.ConnectorSpecification;
import amp.topology.factory.Modifications;
import amp.topology.global.Connector;
import amp.topology.global.Partition;
import amp.topology.global.impl.BaseGroup;
import amp.topology.global.impl.BaseConnector;
import amp.topology.global.TopicRegistry;
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

    private ConnectorFactory connectorFactory;

    private TopicRegistry topicRegistry;

    public void setConnectorFactory(ConnectorFactory connectorFactory) {

        this.connectorFactory = connectorFactory;
    }

    public void setTopicRegistry(TopicRegistry topicRegistry) {

        this.topicRegistry = topicRegistry;
    }

    @PUT
    @ApiOperation(
            value = "Adds a BaseConnector to a BaseTopic in the Global Topology.",
            notes = "Adds a BaseConnector the the supplied specifications to a BaseTopic in the Global Topology.",
            response = BaseGroup.class,
            authorizations = "gts-connectors-add"
    )
    @Timed
    public Connector<?, ?> add(@Versioned ConnectorSpecification specification) throws Exception {

        return connectorFactory.create(specification);
    }

    @DELETE
    @Path("/{topicId}/{connectorId}")
    @ApiOperation(
            value = "Remove a BaseConnector from the Global Topology.",
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
            notes = "Retrieves all Connectors for a BaseTopic with the given ID in the Global Topology.",
            authorizations = "gts-connectors-list",
            response = ConnectorsCollection.class
    )
    @ApiResponses({
            @ApiResponse(code=404, message="No topic with specified id.")
    })
    @Timed
    @Metered(name="connectors-list")
    public ConnectorsCollection list(@PathParam("topicId") String topicId) throws Exception {

        amp.topology.global.Topic topic = this.topicRegistry.get(topicId);

        return new ConnectorsCollection(topic);
    }

    @GET
    @Path("/{topicId}/{connectorId}")
    @ApiOperation(
            value = "Retrieve a specific connector from the Global Topology.",
            notes = "Retrieves a BaseConnector for a BaseTopic with the given ID in the Global Topology.",
            authorizations = "gts-groups-list",
            response = BaseConnector.class
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
     * Wraps a BaseConnector Collection in a transportable object.
     */
    @XmlRootElement
    public static class ConnectorsCollection  {

        private final Collection<Connector<? extends Partition, ? extends Partition>> connectors;

        public ConnectorsCollection(amp.topology.global.Topic topicConfiguration) {

            this.connectors = topicConfiguration.getConnectors();
        }

        public Collection<Connector<? extends Partition, ? extends Partition>> getConnectors() {

            return connectors;
        }
    }
}
