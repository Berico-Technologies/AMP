package amp.topology.global.lifecycle;

import amp.topology.global.*;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class Hydrater {

    @SuppressWarnings("unchecked")
    public static <T extends Topic> T  hydrate(Topic.HydratedState state) throws Exception {

        T topic = (T)state.getTopologyItemType().newInstance();

        topic.restore(state);

        for(String id : state.getProducerGroupIds()){

            TopologyGroup.HydratedState groupState = PersistenceManager.groups().get(id);

            topic.addProducerGroup((ProducerGroup<?>) hydrate(groupState));
        }

        for(String id : state.getConsumerGroupIds()){

            TopologyGroup.HydratedState groupState = PersistenceManager.groups().get(id);

            topic.addConsumerGroup((ConsumerGroup<?>) hydrate(groupState));
        }

        for (String id : state.getConnectorIds()){

            Connector.HydratedState connectorState = PersistenceManager.connectors().get(id);

            topic.addConnector(hydrate(connectorState, topic));
        }

        return topic;
    }

    @SuppressWarnings("unchecked")
    public static <T extends TopologyGroup> T  hydrate(TopologyGroup.HydratedState state) throws Exception {

        T group = (T)state.getTopologyItemType().newInstance();

        //group.restore(state);

        for (String id : state.getPartitionIds()){

            Partition.HydratedState partitionState = PersistenceManager.partitions().get(id);

            group.addPartition(hydrate(partitionState));
        }

        return group;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Connector> T  hydrate(Connector.HydratedState state) throws Exception {

        T connector = (T)state.getTopologyItemType().newInstance();

        //connector.restore(state);

        TopologyGroup.HydratedState pgroupState = PersistenceManager.groups().get(state.getProducerGroupId());

        ProducerGroup<?> pgroup = hydrate(pgroupState);

        connector.setProducerGroup(pgroup);

        TopologyGroup.HydratedState cgroupState = PersistenceManager.groups().get(state.getConsumerGroupId());

        ConsumerGroup<?> cgroup = hydrate(pgroupState);

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
    public static <T extends Connector> T  hydrate(Connector.HydratedState state, Topic topic) throws Exception {

        T connector = (T)state.getTopologyItemType().newInstance();

        //connector.restore(state);

        connector.setConsumerGroup( topic.getConsumerGroup( state.getConsumerGroupId() ) );

        connector.setProducerGroup( topic.getProducerGroup( state.getProducerGroupId() ) );

        return connector;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Partition> T  hydrate(Partition.HydratedState state) throws Exception {

        T partition = (T)state.getTopologyItemType().newInstance();

        //partition.restore(state);

        return partition;
    }

}
