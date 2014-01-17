package amp.topology.protocols.rabbit.factories;

import amp.topology.factory.GroupSpecification;
import amp.topology.factory.Modifications;
import amp.topology.factory.impl.DelegatingGroupFactory;
import amp.topology.global.TopologyGroup;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class RabbitPubSubGroupFactory implements DelegatingGroupFactory.GroupFactoryDelegate {

    @Override
    public boolean canHandle(GroupSpecification specification) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public TopologyGroup<?> createGroup(GroupSpecification specification) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Modifications modify(TopologyGroup<?> topologyGroup, GroupSpecification specification) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
