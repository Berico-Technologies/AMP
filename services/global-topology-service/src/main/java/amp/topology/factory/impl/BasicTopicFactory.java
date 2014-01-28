package amp.topology.factory.impl;

import amp.topology.Constants;
import amp.topology.factory.*;
import amp.topology.factory.specifications.ConnectorSpecification_3_3_0;
import amp.topology.factory.specifications.GroupSpecification_3_3_0;
import amp.topology.global.impl.BaseTopic;
import amp.topology.global.TopicRegistry;

/**
 * Creates BaseTopic from TopicSpecification
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class BasicTopicFactory implements TopicFactory {

    TopicRegistry topicRegistry;

    GroupFactory groupFactory;

    ConnectorFactory connectorFactory;

    /**
     * Instantiate the BasicTopicFactory with it's dependent services.
     * @param topicRegistry BaseTopic Registry
     * @param groupFactory BaseGroup Factory
     * @param connectorFactory BaseConnector Factory
     */
    public BasicTopicFactory(
            TopicRegistry topicRegistry, GroupFactory groupFactory, ConnectorFactory connectorFactory) {

        this.topicRegistry = topicRegistry;
        this.groupFactory = groupFactory;
        this.connectorFactory = connectorFactory;
    }

    @Override
    public amp.topology.global.Topic create(TopicSpecification specification) throws Exception {

        BaseTopic topic =
                new BaseTopic(specification.getTopicId(), specification.getDescription());

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

        amp.topology.global.Topic topicConfiguration = topicRegistry.get(specification.getTopicId());

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
                "Default Producer BaseGroup",
                null,
                null,
                "default-producer",
                false,
                Constants.PROTOCOL_AMQP);
    }

    GroupSpecification getDefaultConsumerGroupForTopic(TopicSpecification specification){

        return new GroupSpecification_3_3_0(
                specification.getTopicId(),
                "Default Consumer BaseGroup",
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
                "Default BaseConnector",
                null,
                null,
                "default-connector",
                producerGroupId,
                consumerGroupId);
    }
}
