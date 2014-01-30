package amp.topology.resources;

import amp.topology.factory.GroupFactory;
import amp.topology.factory.GroupSpecification;
import amp.topology.factory.Modifications;
import amp.topology.global.*;
import amp.topology.global.impl.BaseGroup;
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
@Path("topology/groups")
@Api(
        value = "service/topology/groups",
        description = "Manage groups in the Global Topology."
)
@Produces({ "application/json;qs=1", "application/xml;qs=0.5" })
public class GroupResource {

    GroupFactory groupFactory;

    TopicRegistry topicRegistry;

    public void setGroupFactory(GroupFactory groupFactory) {

        this.groupFactory = groupFactory;
    }

    public void setTopicRegistry(TopicRegistry topicRegistry) {

        this.topicRegistry = topicRegistry;
    }

    @PUT
    @ApiOperation(
        value = "Adds a BaseGroup to a BasicTopic in the Global Topology.",
        notes = "Adds a BaseGroup the the supplied specifications to a BasicTopic in the Global Topology.",
        response = BaseGroup.class,
        authorizations = "gts-groups-add"
    )
    @Timed
    public amp.topology.global.Group<? extends Partition> add(@Versioned GroupSpecification specification) throws Exception {

        return groupFactory.create(specification);
    }

    @DELETE
    @Path("/{topicId}/{groupId}")
    @ApiOperation(
            value = "Remove a BaseGroup from the Global Topology.",
            notes = "Removes the group with the given ID from the Global Topology, cleaning up resources as needed.",
            authorizations = "gts-groups-remove"
    )
    @ApiResponses({
            @ApiResponse(code=404, message="No topic with specified id."),
            @ApiResponse(code=404, message="No group with specified id.")
    })
    @Timed
    public Response remove(@PathParam("topicId") String topicId, @PathParam("groupId") String groupId) throws Exception {

        topicRegistry.get(topicId).removeGroup(groupId);

        return Response.ok().build();
    }

    @GET
    @Path("/{topicId}")
    @ApiOperation(
            value = "Retrieve all groups from the Global Topology.",
            notes = "Retrieves all Producing and Consuming groups for a BasicTopic with the given ID in the Global Topology.",
            authorizations = "gts-groups-list",
            response = TopologyGroupCollection.class
    )
    @ApiResponses({
            @ApiResponse(code=404, message="No topic with specified id.")
    })
    @Timed
    @Metered(name="group-list")
    public TopologyGroupCollection list(@PathParam("topicId") String topicId) throws Exception {

        amp.topology.global.Topic topic = topicRegistry.get(topicId);

        return new TopologyGroupCollection(topic);
    }

    @GET
    @Path("/{topicId}/{groupId}")
    @ApiOperation(
            value = "Retrieve a specific group from the Global Topology.",
            notes = "Retrieves a Producing or Consuming group for a BasicTopic with the given IDs in the Global Topology.",
            authorizations = "gts-groups-get",
            response = BaseGroup.class
    )
    @ApiResponses({
            @ApiResponse(code=404, message="No topic with specified id."),
            @ApiResponse(code=404, message="No group with specified id.")
    })
    @Timed
    @Metered(name="group-get")
    public amp.topology.global.Group<?> get(@PathParam("topicId") String topicId, @PathParam("groupId") String groupId) throws Exception {

        return topicRegistry.get(topicId).getGroup(groupId);
    }

    @POST
    @ApiOperation(
            value = "Modify a group in the Global Topology.",
            notes = "Modifies an existing group with the information from the supplied GroupSpecification.",
            authorizations = "gts-groups-modify",
            response = Modifications.class
    )
    @ApiResponses({
            @ApiResponse(code=404, message="No topic with specified id."),
            @ApiResponse(code=404, message="No group with specified id.")
    })
    @Timed
    public Modifications modify(@Versioned GroupSpecification specification) throws Exception {

        return this.groupFactory.modify(specification);
    }


    /**
     * Represents all the Producer and Consumer Groups in a BasicTopic.
     */
    @XmlRootElement
    public static class TopologyGroupCollection {

        private final Collection<ProducerGroup<?>> producerGroups;

        private final Collection<ConsumerGroup<?>> consumerGroups;

        public TopologyGroupCollection(
                amp.topology.global.Topic topicConfiguration) {

            this.producerGroups = topicConfiguration.getProducerGroups();
            this.consumerGroups = topicConfiguration.getConsumerGroups();
        }

        public Collection<ProducerGroup<?>> getProducerGroups() {
            return producerGroups;
        }

        public Collection<ConsumerGroup<?>> getConsumerGroups() {
            return consumerGroups;
        }
    }
}
