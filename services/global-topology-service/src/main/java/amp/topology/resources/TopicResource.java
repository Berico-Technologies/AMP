package amp.topology.resources;

import amp.topology.factory.Modifications;
import amp.topology.factory.TopicFactory;
import amp.topology.factory.TopicSpecification;
import amp.topology.global.Topic;
import amp.topology.global.TopicRegistry;
import amp.topology.resources.common.Versioned;
import com.google.common.collect.Iterables;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.annotation.Timed;

import javax.ws.rs.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.Collection;

/**
 * Endpoint for managing Topics in the Global Topology.
 *
 * @author Richard Clayton (Berico Technologies)
 */
@Path("topology/topics")
@Api(
        value = "service/topology/topics",
        description = "Manage topics in the Global Topology."
)
@Produces({ "application/json;qs=1", "application/xml;qs=0.5" })
public class TopicResource {

    TopicFactory topicFactory;

    TopicRegistry topicRegistry;

    /**
     * Set the TopicFactory used to create new TopicConfigurations.
     * @param topicFactory TopicFactory to set.
     */
    public void setTopicFactory(TopicFactory topicFactory) {

        this.topicFactory = topicFactory;
    }

    /**
     * Set the TopicRegistry used to manage the lifecycle of Topics.
     * @param topicRegistry TopicRegsitry to set.
     */
    public void setTopicRegistry(TopicRegistry topicRegistry) {

        this.topicRegistry = topicRegistry;
    }

    @POST
    @ApiOperation(
        value = "Add a Topic to the Global Topology",
        notes = "Adds a Topic with the supplied specification to the Global Topology, provisioning resources as needed.",
        response = Topic.class,
        authorizations = "gts-topic-add"
    )
    @Timed
    public Topic add(@Versioned TopicSpecification specification) throws Exception {

        return this.topicFactory.create(specification);
    }

    @DELETE
    @Path("/{id}")
    @ApiOperation(
        value = "Remove a Topic from the Global Topology.",
        notes = "Removes the topic with the given ID from the Global Topology, cleaning up resources as needed.",
        authorizations = "gts-topic-remove"
    )
    @ApiResponses({
        @ApiResponse(code=404, message="No topic with specified id.")
    })
    @Timed
    public void remove(@PathParam("id") String id) throws Exception {

        this.topicRegistry.unregister(id);
    }

    @GET
    @ApiOperation(
        value = "Get all Topics in the Global Topology.",
        notes = "Retrieves all Topics in the Global Topology, though it may filter some based on access controls.",
        response = TopicConfigurationCollection.class,
        authorizations = "gts-topic-list"
    )
    @Timed
    @Metered(name="list-meter")
    public TopicConfigurationCollection list() throws Exception {

        Collection<Topic> topics =
                Arrays.asList(
                        Iterables.toArray( this.topicRegistry.entries(), Topic.class ));

        return new TopicConfigurationCollection(topics);
    }

    @GET
    @Path("/{id}")
    @ApiOperation(
        value = "Get a Topic in the Global Topology.",
        notes = "Retrieves a topic registered in the Global Topology.",
        response = Topic.class,
        authorizations = "gts-topic-get"
    )
    @ApiResponses({
            @ApiResponse(code=404, message="No topic with specified id.")
    })
    @Metered
    public Topic get(@PathParam("id") String topicId) throws Exception {

        return topicRegistry.get(topicId);
    }

    @POST
    @ApiOperation(
            value = "Modify a Topic in the Global Topology",
            notes = "Modify a Topic in the Global Topology using the supplied specification.",
            authorizations = "gts-topic-modify",
            response = Modifications.class
    )
    @ApiResponses({
            @ApiResponse(code=404, message="No topic with specified id.")
    })
    @Timed
    public Modifications modify(@Versioned TopicSpecification specification) throws Exception {

        return topicFactory.modify(specification);
    }

    /**
     * A Wrapper for Topics.
     */
    @XmlRootElement
    public static class TopicConfigurationCollection {

        private final Collection<Topic> topics;

        public TopicConfigurationCollection(Collection<Topic> topics) {

            this.topics = topics;
        }

        public Collection<Topic> getTopics() {

            return topics;
        }
    }
}
