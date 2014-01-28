package amp.topology.global;

import amp.topology.global.exceptions.ConnectorNotExistException;
import amp.topology.global.exceptions.TopologyGroupNotExistException;
import amp.topology.global.filtering.RouteFilterResults;
import amp.topology.global.filtering.RouteRequirements;
import amp.topology.global.impl.BaseConnector;
import amp.topology.global.impl.BaseProducerGroup;
import amp.topology.global.impl.BaseConsumerGroup;

import java.util.Collection;
import java.util.Map;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public interface Topic extends TopologyItem {

    RouteFilterResults filter(RouteRequirements requirements);

    /**
     * Specifies the state of a group's existence.
     */
    public enum GroupExists { AsProducer, AsConsumer, False }

    boolean groupExists(String id);

    GroupExists getGroupExists(String id);

    Group<? extends Partition> getGroup(String id) throws Exception;

    void addGroup(Group<? extends Partition> group) throws Exception;

    void removeGroup(String id) throws Exception;


    ProducerGroup<? extends Partition> getProducerGroup(String id) throws TopologyGroupNotExistException;

    Collection<ProducerGroup<? extends Partition>> getProducerGroups();



    ConsumerGroup<? extends Partition> getConsumerGroup(String id) throws TopologyGroupNotExistException;

    Collection<ConsumerGroup<? extends Partition>> getConsumerGroups();



    void addConnector(Connector<? extends Partition, ? extends Partition> connector) throws Exception;

    void removeConnector(String id) throws Exception;

    Connector<? extends Partition, ? extends Partition> getConnector(String id)
            throws ConnectorNotExistException;

    Collection<Connector<? extends Partition, ? extends Partition>> getConnectors();
}
