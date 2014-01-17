package amp.topology.factory.impl;

import amp.topology.Constants;
import amp.topology.factory.*;
import amp.topology.factory.specifications.ConnectorSpecification_3_3_0;
import amp.topology.factory.specifications.GroupSpecification_3_3_0;
import amp.topology.global.TopicConfiguration;
import amp.topology.global.TopicRegistry;
import amp.topology.global.impl.BasicTopicConfiguration;

/**
 * Creates BasicTopicConfiguration from TopicSpecification
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class BasicTopicFactory implements TopicFactory {

    TopicRegistry topicRegistry;

    GroupFactory groupFactory;

    ConnectorFactory connectorFactory;

    /**
     * Instantiate the BasicTopicFactory with it's dependent services.
     * @param topicRegistry Topic Registry
     * @param groupFactory Group Factory
     * @param connectorFactory Connector Factory
     */
    public BasicTopicFactory(
            TopicRegistry topicRegistry, GroupFactory groupFactory, ConnectorFactory connectorFactory) {

        this.topicRegistry = topicRegistry;
        this.groupFactory = groupFactory;
        this.connectorFactory = connectorFactory;
    }

    @Override
    public TopicConfiguration create(TopicSpecification specification) throws Exception {

        BasicTopicConfiguration topic =
                new BasicTopicConfiguration(specification.getTopicId(), specification.getDescription());

        topicRegistry.register(topic);

        if (specification.shouldCreateDefaults()){

            GroupSpecification pgroupSpecification = getDefaultProducerGroupForTopic(specification);

            groupFactory.create(pgroupSpecification);

            GroupSpecification cgroupSpecification = getDefaultConsumerGroupForTopic(specification);

            groupFactory.create(cgroupSpecification);

            ConnectorSpecification connectorSpecification =
                    getDefaultConnectorForTopic(
                            specification, pgroupSpecification.getGroupId(), cgroupSpecification.getGroupId());

            connectorFactory.create(connectorSpecification);
        }

        return topic;
    }

    @Override
    public Modifications modify(TopicSpecification specification) throws Exception {

        TopicConfiguration topicConfiguration = topicRegistry.get(specification.getTopicId());

        Modifications modifications = new Modifications();

        if (!specification.getDescription().equals(topicConfiguration.getDescription())) {

            String oldValue = topicConfiguration.getDescription();

            topicConfiguration.setDescription(specification.getDescription());

            modifications.add(new Modifications.Entry("description", true, oldValue, specification.getDescription()));
        }

        return modifications;
    }

    GroupSpecification getDefaultProducerGroupForTopic(TopicSpecification specification){

        return new GroupSpecification_3_3_0(
                specification.getTopicId(),
                "Default Producer Group",
                null,
                null,
                "default-producer",
                false,
                Constants.PROTOCOL_AMQP);
    }

    GroupSpecification getDefaultConsumerGroupForTopic(TopicSpecification specification){

        return new GroupSpecification_3_3_0(
                specification.getTopicId(),
                "Default Consumer Group",
                null,
                null,
                "default-consumer",
                false,
                Constants.PROTOCOL_AMQP);
    }

    ConnectorSpecification getDefaultConnectorForTopic(
            TopicSpecification specification, String producerGroupId, String consumerGroupId){

        return new ConnectorSpecification_3_3_0(
                specification.getTopicId(),
                "Default Connector",
                null,
                null,
                "default-connector",
                producerGroupId,
                consumerGroupId);
    }
}
