package amp.topology.factory.impl;

import amp.topology.factory.ConnectorFactory;
import amp.topology.factory.ConnectorSpecification;
import amp.topology.factory.Modifications;
import amp.topology.global.*;
import amp.topology.global.impl.*;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class DelegatingConnectorFactory implements ConnectorFactory {

    TopicRegistry topicRegistry;

    List<ConnectorFactoryDelegate> factoryDelegates = Lists.newCopyOnWriteArrayList();

    public DelegatingConnectorFactory(TopicRegistry topicRegistry) {

        this.topicRegistry = topicRegistry;
    }

    public DelegatingConnectorFactory(TopicRegistry topicRegistry, Collection<ConnectorFactoryDelegate> factoryDelegates) {

        this.topicRegistry = topicRegistry;
        this.factoryDelegates.addAll(factoryDelegates);
    }

    public void setFactoryDelegates(Collection<ConnectorFactoryDelegate> factoryDelegates) {

        this.factoryDelegates.addAll(factoryDelegates);
    }

    @Override
    public Connector<? extends Partition, ? extends Partition>
                create(ConnectorSpecification specification) throws Exception {

        amp.topology.global.Topic topicConfiguration = this.topicRegistry.get(specification.getTopicId());

        ProducerGroup<?> producerGroup = topicConfiguration
                .getProducerGroup(specification.getProducerGroupId());

        ConsumerGroup<?> consumerGroup = topicConfiguration
                .getConsumerGroup(specification.getConsumerGroupId());

        ConnectorFactoryDelegate delegate = selectDelegate(specification);

        Connector<?, ?> connector = delegate.createConnector(specification, producerGroup, consumerGroup);

        topicConfiguration.addConnector(connector);

        return connector;
    }

    @Override
    public Modifications modify(ConnectorSpecification specification) throws Exception {

        Connector<?, ?> connector =  topicRegistry
                .get(specification.getTopicId())
                .getConnector(specification.getConnectorId());

        ConnectorFactoryDelegate delegate = selectDelegate(specification);

        return delegate.modify(connector, specification);
    }

    ConnectorFactoryDelegate selectDelegate(ConnectorSpecification specification)
            throws NoConnectorFactoryAvailableForSpecificationException {

        for (ConnectorFactoryDelegate delegate : factoryDelegates)
            if (delegate.canHandle(specification)) return delegate;

        throw new NoConnectorFactoryAvailableForSpecificationException(specification);
    }

    public interface ConnectorFactoryDelegate {

        boolean canHandle(ConnectorSpecification specification);

        Connector<?, ?> createConnector(
                ConnectorSpecification specification,
                ProducerGroup producingGroup,
                ConsumerGroup consumingGroup);

        Modifications modify(Connector<?, ?> connector, ConnectorSpecification specification);

    }
}
