package amp.topology.resources;

import amp.topology.factory.ConnectorFactory;
import amp.topology.factory.GroupFactory;
import amp.topology.factory.TopicFactory;
import amp.topology.global.TopicRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

/**
 * Wires up the JAX-RS Endpoints for the managing the Global Topology.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class ResourceConfig {

    /**
     * This should be defined somewhere in a Spring Context file.
     */
    @Autowired
    TopicRegistry topicRegistry;

    /**
     * This should be defined somewhere in a Spring Context file.
     */
    @Autowired
    TopicFactory topicFactory;

    /**
     * This should be defined somewhere in a Spring Context file.
     */
    @Autowired
    GroupFactory groupFactory;

    /**
     * This should be defined somewhere in a Spring Context file.
     */
    @Autowired
    ConnectorFactory connectorFactory;


    @Bean(name = "topicResource")
    public TopicResource getTopicResource(){

        TopicResource resource = new TopicResource();

        resource.setTopicRegistry(topicRegistry);

        resource.setTopicFactory(topicFactory);

        return resource;
    }

    @Bean(name = "groupResource")
    public GroupResource getGroupResource(){

        GroupResource resource = new GroupResource();

        resource.setTopicRegistry(topicRegistry);

        resource.setGroupFactory(groupFactory);

        return resource;
    }

    @Bean(name = "connectorResource")
    public ConnectorResource getConnectorResource(){

        ConnectorResource resource = new ConnectorResource();

        resource.setTopicRegistry(topicRegistry);

        resource.setConnectorFactory(connectorFactory);

        return resource;
    }
}
