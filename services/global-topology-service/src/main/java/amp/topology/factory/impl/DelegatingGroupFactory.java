package amp.topology.factory.impl;

import amp.topology.factory.GroupFactory;
import amp.topology.factory.GroupSpecification;
import amp.topology.factory.Modifications;
import amp.topology.global.*;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;

/**
 * Delegated construction and modification of Groups to delegate factories.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class DelegatingGroupFactory implements GroupFactory {

    TopicRegistry topicRegistry;

    List<GroupFactoryDelegate> factoryDelegates = Lists.newCopyOnWriteArrayList();

    public DelegatingGroupFactory(TopicRegistry topicRegistry) {

        this.topicRegistry = topicRegistry;
    }

    public DelegatingGroupFactory(TopicRegistry topicRegistry, Collection<GroupFactoryDelegate> factoryDelegateCollection){

        this.topicRegistry = topicRegistry;

        this.factoryDelegates.addAll(factoryDelegateCollection);
    }

    public void setFactoryDelegates(Collection<GroupFactoryDelegate> delegates){

        this.factoryDelegates.addAll(delegates);
    }


    @Override
    public TopologyGroup<? extends Partition> create(GroupSpecification specification) throws Exception {

        TopicConfiguration topic = topicRegistry.get(specification.getTopicId());

        GroupFactoryDelegate delegate = selectDelegate(specification);

        TopologyGroup<?> group = delegate.createGroup(specification);

        if (ProducerGroup.class.isAssignableFrom(group.getClass()))

            topic.addProducerGroup((ProducerGroup) group);

        else if (ConsumerGroup.class.isAssignableFrom(group.getClass()))

            topic.addConsumerGroup((ConsumerGroup) group);

        else

            throw new RuntimeException(
                    String.format(
                        "Group [%s] is not a ProducingGroup or ConsumingGroup.  " +
                        "This is very bad.  While we supply super type to help reduce " +
                        "the coding burden on developers, implementations must implement one of " +
                        "the aforementioned interfaces.", group.getClass()));

        return group;
    }

    @Override
    public Modifications modify(GroupSpecification specification) throws Exception {

        TopologyGroup<?> group = topicRegistry
                .get(specification.getTopicId())
                .getGroup(specification.getGroupId());

        GroupFactoryDelegate delegate = selectDelegate(specification);

        return delegate.modify(group, specification);
    }

    GroupFactoryDelegate selectDelegate(GroupSpecification specification)
            throws NoGroupFactoryAvailableForSpecificationException {

        for (GroupFactoryDelegate delegate : factoryDelegates)
            if (delegate.canHandle(specification)) return delegate;

        throw new NoGroupFactoryAvailableForSpecificationException(specification);
    }

    public interface GroupFactoryDelegate {

        boolean canHandle(GroupSpecification specification);

        TopologyGroup<?> createGroup(GroupSpecification specification);

        Modifications modify(TopologyGroup<?> topologyGroup, GroupSpecification specification);

    }
}
