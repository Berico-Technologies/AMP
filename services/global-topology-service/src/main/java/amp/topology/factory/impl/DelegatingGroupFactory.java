package amp.topology.factory.impl;

import amp.topology.factory.GroupFactory;
import amp.topology.factory.GroupSpecification;
import amp.topology.factory.Modifications;
import amp.topology.global.*;
import amp.topology.global.impl.*;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;

/**
 * Delegated construction and modification of Groups to delegate factories.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class DelegatingGroupFactory implements GroupFactory {

    private TopicRegistry topicRegistry;

    private List<GroupFactoryDelegate> factoryDelegates = Lists.newCopyOnWriteArrayList();

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
    public Group<? extends Partition> create(GroupSpecification specification) throws Exception {

        amp.topology.global.Topic topic = topicRegistry.get(specification.getTopicId());

        GroupFactoryDelegate delegate = selectDelegate(specification);

        amp.topology.global.Group<?> group = delegate.createGroup(specification);

        topic.addGroup(group);

        return group;
    }

    @Override
    public Modifications modify(GroupSpecification specification) throws Exception {

        amp.topology.global.Group<?> group = topicRegistry
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

        amp.topology.global.Group<?> createGroup(GroupSpecification specification);

        Modifications modify(amp.topology.global.Group<?> group, GroupSpecification specification);

    }
}
