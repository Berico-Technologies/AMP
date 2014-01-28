package amp.topology.global.persistence;

import amp.topology.global.*;
import amp.topology.global.impl.*;
import amp.topology.global.impl.BaseTopic;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class Hydrater {

    @SuppressWarnings("unchecked")
    public static <T extends BaseTopic> T  hydrate(BaseTopic.DehydratedState state) throws Exception {

        T topic = (T)state.getTopologyItemType().newInstance();

        topic.restore(state);

        Set<ProducerGroup<? extends Partition>> pgroups = Sets.newHashSet();

        for(String id : state.getProducerGroupIds()){

            BaseGroup.DehydratedState groupState = PersistenceManager.groups().get(id);

            pgroups.add((ProducerGroup<? extends Partition>)hydrate(groupState));
        }

        topic.setProducerGroups(pgroups);

        Set<ConsumerGroup<? extends Partition>> cgroups = Sets.newHashSet();

        for(String id : state.getConsumerGroupIds()){

            BaseGroup.DehydratedState groupState = PersistenceManager.groups().get(id);

            cgroups.add((ConsumerGroup<? extends Partition>)hydrate(groupState));
        }

        topic.setConsumerGroups(cgroups);

        Set<Connector<? extends Partition, ? extends Partition>> connectors = Sets.newHashSet();

        for (String id : state.getConnectorIds()){

            BaseConnector.DehydratedState connectorState = PersistenceManager.connectors().get(id);

            connectors.add(hydrate(connectorState, topic));
        }

        topic.setConnectors(connectors);

        return topic;
    }

    @SuppressWarnings("unchecked")
    public static <T extends BaseGroup> T  hydrate(BaseGroup.DehydratedState state) throws Exception {

        T group = (T)state.getTopologyItemType().newInstance();

        group.restore(state);

        Set<Partition> partitions = Sets.newHashSet();

        for (String id : state.getPartitionIds()){

            BasePartition.DehydratedState partitionState = PersistenceManager.partitions().get(id);

            partitions.add(hydrate(partitionState));
        }

        group.setPartitions(partitions);

        return group;
    }

    @SuppressWarnings("unchecked")
    public static <T extends BaseConnector> T  hydrate(BaseConnector.DehydratedState state) throws Exception {

        T connector = (T)state.getTopologyItemType().newInstance();

        connector.restore(state);

        BaseGroup.DehydratedState pgroupState = PersistenceManager.groups().get(state.getProducerGroupId());

        BaseProducerGroup<?> pgroup = hydrate(pgroupState);

        connector.setProducerGroup(pgroup);

        BaseGroup.DehydratedState cgroupState = PersistenceManager.groups().get(state.getConsumerGroupId());

        BaseConsumerGroup<?> cgroup = hydrate(cgroupState);

        connector.setConsumerGroup(cgroup);

        return connector;
    }

    /**
     * A special form of hydrate that picks up already hydrated instance of TopologyGroups instead of creating
     * new instances.
     * @param state
     * @param topic
     * @param <T>
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static <T extends BaseConnector> T  hydrate(BaseConnector.DehydratedState state, Topic topic) throws Exception {

        T connector = (T)state.getTopologyItemType().newInstance();

        connector.restore(state);

        connector.setConsumerGroup( topic.getConsumerGroup( state.getConsumerGroupId() ) );

        connector.setProducerGroup( topic.getProducerGroup( state.getProducerGroupId() ) );

        return connector;
    }

    @SuppressWarnings("unchecked")
    public static <T extends BasePartition> T  hydrate(BasePartition.DehydratedState state) throws Exception {

        T partition = (T)state.getTopologyItemType().newInstance();

        partition.restore(state);

        return partition;
    }

}
