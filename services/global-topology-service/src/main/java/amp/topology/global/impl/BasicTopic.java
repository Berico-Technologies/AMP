package amp.topology.global.impl;

import amp.topology.global.*;
import amp.topology.global.exceptions.ConnectorAlreadyExistsException;
import amp.topology.global.exceptions.ConnectorNotExistException;
import amp.topology.global.exceptions.TopologyGroupAlreadyExistsException;
import amp.topology.global.exceptions.TopologyGroupNotExistException;
import amp.topology.global.filtering.RouteFilterResults;
import amp.topology.global.filtering.RouteRequirements;
import amp.topology.global.lifecycle.LifeCycleObserver;
import amp.topology.global.persistence.PersistenceManager;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * Container for the routes associated with a particular topology.
 *
 * The BasicTopic is responsible for managing the life cycle of
 * TopologyGroups and Connectors.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class BasicTopic extends BaseTopologyItem<BasicTopic.DehydratedState> implements amp.topology.global.Topic {

    private ConcurrentMap<String, ProducerGroup<?>> pgroups = Maps.newConcurrentMap();

    private ConcurrentMap<String, ConsumerGroup<?>> cgroups = Maps.newConcurrentMap();

    private ConcurrentMap<String, Connector<?, ?>> connectors = Maps.newConcurrentMap();

    /**
     * No! No!  Don't you dare!  This is for state hydration purposes only!
     */
    public BasicTopic(){}

    /**
     * Instantiate the BasicTopic with it's id.
     * @param id A globally unique id in the topic space.  Typically this is the name of an event (canonical class name)
     *           or some easily identified but more generic category (e.g. "user-queues").
     */
    public BasicTopic(String id){

        this.setTopicId(id);
    }

    /**
     * Instantiate the BasicTopic with it's id.
     * @param id A globally unique id in the topic space.  Typically this is the name of an event (canonical class name)
     *           or some easily identified but more generic category (e.g. "user-queues").
     * @param description A friendly description of this topic.
     */
    public BasicTopic(String id, String description) {

        this.setTopicId(id);
        this.setDescription(description);
    }

    /**
     * Filter the Topology entities based on the supplied Route Requirements.
     * @param requirements Requirements of the client for a particular route.
     * @return Filtered Route Results.
     */
    @Override
    public RouteFilterResults filter(RouteRequirements requirements) {

        RouteFilterResults.Builder resultsBuilder = RouteFilterResults.Builder(requirements);

        for (ProducerGroup<?> pgroup : pgroups.values()){

            Collection<? extends amp.topology.global.Partition> applicablePartitions = pgroup.filter(requirements);

            resultsBuilder.produceOn(applicablePartitions);
        }

        for (ConsumerGroup<?> cgroup : cgroups.values()){

            Collection<? extends Partition> applicablePartitions = cgroup.filter(requirements);

            resultsBuilder.consumeOn(applicablePartitions);
        }

        return resultsBuilder.build();
    }

    @Override
    public void setup() throws Exception {
        // Do nothing.
    }

    /**
     * Ensures all groups and connectors are properly cleaned up.
     * @throws Exception Probably an exception occurring during the cleanup process of a BaseGroup, BaseConnector, or BasePartition.
     */
    @Override
    public void cleanup() throws Exception {

        // Second, unbind the connections
        for (Connector<?, ?> connector : connectors.values())
            cleanup(connector);

        connectors.clear();

        // First, stop producers from producing!
        for (ProducerGroup<?> producerGroup : pgroups.values())
            cleanup(producerGroup);

        pgroups.clear();

        // Finally, tell the consumers to stop.
        for (ConsumerGroup<?> consumerGroup : cgroups.values())
            cleanup(consumerGroup);

        cgroups.clear();
    }

    @Override
    public DehydratedState dehydrate() {

        DehydratedState topicState =
                new DehydratedState(
                        getClass(),
                        this.getTopicId(),
                        this.getDescription(),
                        pgroups.keySet(),
                        cgroups.keySet(),
                        connectors.keySet());

        topicState.getExtensionProperties().putAll(getExtensionProperties());

        return topicState;
    }


    /**
     * Persist the BasicTopic state.
     */
    @Override
    public void save(){

        save(true);
    }

    @Override
    public void save(boolean saveAggregates) {

        if (saveAggregates){

            for (Group group : pgroups.values()) group.save();

            for (Group group : cgroups.values()) group.save();

            for (Connector connector : connectors.values()) connector.save();
        }

        LifeCycleObserver.fireOnSaved(this);
    }

    /**
     * Restore the object from the extended properties (supplied by implementations).
     * @param properties Properties used to restore the internal state of the object.
     */
    @Override
    public void set(Map<String, String> properties) {}

    /**
     * Get any properties specific to this implementation that should be stored and
     * retrieved later for initializing the object.
     * @return
     */
    @Override
    public Map<String, String> getExtensionProperties() { return Maps.newHashMap(); }

    /**
     * Get a BaseGroup by it's id; it doesn't matter whether that is a producer or consumer group.
     * @param id ID of the BaseGroup to retrieve.
     * @return BaseGroup
     * @throws Exception Thrown if the group does not exist.
     */
    @Override
    public Group<? extends Partition> getGroup(String id) throws Exception {

        GroupExists groupExists = getGroupExists(id);

        if (groupExists == GroupExists.AsProducer)
            return getProducerGroup(id);

        else if (groupExists == GroupExists.AsConsumer)
            return getConsumerGroup(id);

        else
            throw new TopologyGroupNotExistException(this.getTopicId(), id, false);
    }

    /**
     * Remove a BaseGroup by it's id; it doesn't matter whether the group is a producer or consumer group.
     * @param id ID of the BaseGroup to remove.
     * @throws Exception Thrown if the group doesn't exist, or an error is encountered cleanup resources of that group.
     */
    @Override
    public void removeGroup(String id) throws Exception {

        GroupExists groupExists = getGroupExists(id);

        if (groupExists == GroupExists.AsProducer)
            removeProducerGroup(id);

        else if (groupExists == GroupExists.AsConsumer)
            removeConsumerGroup(id);

        else
            throw new TopologyGroupNotExistException(this.getTopicId(), id, false);
    }

    /**
     * Does the group with the specified id exist?
     * @param id ID of the group to check existence for.
     * @return TRUE if it does exist, FALSE if it does not.
     */
    @Override
    public boolean groupExists(String id) {

        GroupExists groupExists = getGroupExists(id);

        return groupExists != GroupExists.False;
    }

    /**
     * A more complex form of groupExists(id), letting you know whether it's a Producer, Consumer, or Nonexistent.
     * @param id Id of the BaseGroup
     * @return Status of Existence.
     */
    @Override
    public GroupExists getGroupExists(String id){

        if (pgroups.containsKey(id)) return GroupExists.AsProducer;

        if (cgroups.containsKey(id)) return GroupExists.AsConsumer;

        return GroupExists.False;
    }

    @Override
    public void addGroup(Group<? extends Partition> group) throws Exception {

        if (ProducerGroup.class.isAssignableFrom(group.getClass()))
            addProducerGroup((ProducerGroup<? extends Partition>)group);
        else
            addConsumerGroup((ConsumerGroup<? extends Partition>)group);
    }

    /**
     * Add a BaseProducerGroup to the TopicConfiguration.  The "setup" method will be called on the group,
     * and if an exception is raised, the group will fail to be added to the BasicTopic.  All listeners will
     * also be fired for onProducerGroupAdded.
     * @param producerGroup BaseProducerGroup to add.
     * @throws Exception Setup error.
     * @throws TopologyGroupAlreadyExistsException if the group already exists, an exception will be raised.
     */
    void addProducerGroup(ProducerGroup<? extends Partition> producerGroup) throws Exception {

        if (null == pgroups.putIfAbsent(producerGroup.getGroupId(), producerGroup)){

            setup(producerGroup);

            LifeCycleObserver.fireOnAdded(producerGroup);
        }
        else {
            throw new TopologyGroupAlreadyExistsException(this.getTopicId(), producerGroup.getGroupId(), true);
        }
    }

    /**
     * Remove a BaseProducerGroup.  This will call the "cleanup" method on the group, and if an exception is raised,
     * will fail to remove the group.  All listeners will also be fired for onProducerGroupRemoved.
     * @param id Id of the BaseGroup to remove.
     * @throws Exception raised during "cleanup"
     * @throws TopologyGroupNotExistException if the group doesn't exist
     */
     void removeProducerGroup(String id) throws Exception {

         ProducerGroup<?> group = pgroups.remove(id);

         if (group != null){

             cleanup(group);

             LifeCycleObserver.fireOnRemoved(group);
         }
         else {

             throw new TopologyGroupNotExistException(this.getTopicId(), id, true);
         }
    }

    /**
     * Get a BaseProducerGroup by it's id.
     * @param id Id of the BaseProducerGroup
     * @return A BaseProducerGroup
     * @throws TopologyGroupNotExistException if a group does not exist with that id.
     */
    @Override
    public ProducerGroup<? extends Partition> getProducerGroup(String id) throws TopologyGroupNotExistException {

        ProducerGroup<?> group = pgroups.get(id);

        if (group == null) throw new TopologyGroupNotExistException(this.getTopicId(), id, true);

        return group;
    }

    /**
     * Get a unmodifiable collection of the ProducerGroups.
     * @return unmodifiable collection of the ProducerGroups
     */
    @Override
    @SuppressWarnings("unchecked")
    public Collection<ProducerGroup<? extends Partition>> getProducerGroups() {

        return (Collection<ProducerGroup<? extends Partition>>)pgroups.values();
    }

    /**
     * Set producer groups from dehydrated state.
     * @param producerGroups ProducerGroups.
     */
    public void setProducerGroups(Collection<ProducerGroup<? extends Partition>> producerGroups){

        for (ProducerGroup<? extends Partition> producerGroup : producerGroups)
            pgroups.putIfAbsent(producerGroup.getGroupId(), producerGroup);
    }

    /**
     * Add a BaseConsumerGroup to the TopicConfiguration.  The "setup" method will be called on the group,
     * and if an exception is raised, the group will fail to be added to the BasicTopic.  All listeners will
     * also be fired for onConsumerGroupAdded.
     * @param consumerGroup BaseConsumerGroup to add.
     * @throws Exception Setup error.
     * @throws TopologyGroupAlreadyExistsException if the group already exists, an exception will be raised.
     */
    void addConsumerGroup(ConsumerGroup<? extends Partition> consumerGroup) throws Exception {

        if (null == cgroups.putIfAbsent(consumerGroup.getGroupId(), consumerGroup)){

            setup(consumerGroup);

            LifeCycleObserver.fireOnAdded(consumerGroup);
        }
        else {

            throw new TopologyGroupAlreadyExistsException(this.getTopicId(), consumerGroup.getGroupId(), true);
        }
    }

    /**
     * Remove a BaseConsumerGroup.  This will call the "cleanup" method on the group, and if an exception is raised,
     * will fail to remove the group.  All listeners will also be fired for onConsumerGroupRemoved.
     * @param id Id of the BaseGroup to remove.
     * @throws Exception raised during "cleanup"
     * @throws TopologyGroupNotExistException if the group doesn't exist
     */
    void removeConsumerGroup(String id) throws Exception {

        ConsumerGroup<?> group = cgroups.remove(id);

        if (group != null){

            cleanup(group);

            LifeCycleObserver.fireOnRemoved(group);
        }
        else {

            throw new TopologyGroupNotExistException(this.getTopicId(), id, true);
        }
    }

    /**
     * Get a BaseConsumerGroup by it's id.
     * @param id Id of the BaseConsumerGroup.
     * @return A BaseConsumerGroup with the supplied Id.
     * @throws TopologyGroupNotExistException Thrown if a BaseGroup with that Id does not exist.
     */
    @Override
    public ConsumerGroup<? extends Partition> getConsumerGroup(String id) throws TopologyGroupNotExistException {

        ConsumerGroup<?> group = cgroups.get(id);

        if (group == null) throw new TopologyGroupNotExistException(this.getTopicId(), id, true);

        return group;
    }

    /**
     * Get an unmodifiable collection of the ConsumerGroups.
     * @return unmodifiable collection of the ConsumerGroups.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Collection<ConsumerGroup<? extends Partition>> getConsumerGroups() {

        return (Collection<ConsumerGroup<? extends Partition>>)cgroups.values();
    }

    /**
     * Set consumer groups from dehydrated state.
     * @param consumerGroups ConsumerGroups.
     */
    public void setConsumerGroups(Collection<ConsumerGroup<? extends Partition>> consumerGroups){

        for (ConsumerGroup<? extends Partition> consumerGroup : consumerGroups)
            cgroups.putIfAbsent(consumerGroup.getGroupId(), consumerGroup);
    }

    /**
     * Add a BaseConnector to the BasicTopic.  This will call "setup" on the connector, which may error.  In the event
     * of an error, the connector will not be added.  Listeners for onConnectorAdded will also be called.
     * @param connector BaseConnector to add.
     * @throws Exception If an error occurs during "setup"
     * @throws ConnectorAlreadyExistsException if the connector already exists.
     */
    @Override
    public void addConnector(Connector<? extends Partition, ? extends Partition> connector) throws Exception {

        if (null == connectors.putIfAbsent(connector.getConnectorId(), connector)){

            setup(connector);

            LifeCycleObserver.fireOnAdded(connector);
        }
        else {

            throw new ConnectorAlreadyExistsException(this.getTopicId(), connector.getConnectorId());
        }
    }

    /**
     * Remove a connector by it's id.  This will also "cleanup" this connector, and fire all onConnectorRemoved
     * on all listeners.  An exception encountered during the cleanup process will prevent the connector from
     * being removed.
     * @param id Id of the connector to remove.
     * @throws Exception Encountered during "cleanup"
     * @throws ConnectorNotExistException if the connector does not exist.
     */
    @Override
    public void removeConnector(String id) throws Exception {

        Connector<?, ?> connector = connectors.remove(id);

        if (connector != null){

            cleanup(connector);

            LifeCycleObserver.fireOnRemoved(connector);
        }
        else {

            throw new ConnectorNotExistException(this.getTopicId(), id);
        }
    }

    /**
     * Get a BaseConnector by it's id.
     * @param id Id of the BaseConnector.
     * @return BaseConnector
     * @throws ConnectorNotExistException if a connector with the specified Id does not exist.
     */
    @Override
    public Connector<? extends Partition, ? extends Partition> getConnector(String id)
            throws ConnectorNotExistException {

        Connector<?, ?> connector = connectors.get(id);

        if (connector == null) throw new ConnectorNotExistException(this.getTopicId(), id);

        return connector;
    }

    /**
     * Returns an unmodifiable collection of Connectors managed by this TopicConfiguration instance.
     * @return unmodifiable collection of Connectors.
     */
    @Override
    public Collection<Connector<? extends Partition, ? extends Partition>> getConnectors() {

        return (Collection<Connector<? extends Partition, ? extends Partition>>)connectors.values();
    }

    /**
     * Set connectors from dehydrated storage.
     * @param connectors Connectors.
     */
    public void setConnectors(Collection<Connector<? extends Partition, ? extends Partition>> connectors){

        for (Connector<? extends Partition, ? extends Partition> connector : connectors)
            this.connectors.putIfAbsent(connector.getConnectorId(), connector);
    }

    static void cleanup(TopologyItem item) throws Exception {

        if (BaseTopologyItem.class.isAssignableFrom(item.getClass())){

            BaseTopologyItem baseTopologyItem = (BaseTopologyItem)item;

            baseTopologyItem.cleanup();
        }
    }

    static void setup(TopologyItem item) throws Exception {

        if (BaseTopologyItem.class.isAssignableFrom(item.getClass())){

            BaseTopologyItem baseTopologyItem = (BaseTopologyItem)item;

            baseTopologyItem.setup();
        }
    }


    /**
     * Represents the mandatory properties for a BasicTopic.
     */
    public static class DehydratedState extends TopologyState {

        private Set<String> producerGroupIds = Sets.newHashSet();

        private Set<String> consumerGroupIds = Sets.newHashSet();

        private Set<String> connectorIds = Sets.newHashSet();

        public DehydratedState(
                Class<? extends amp.topology.global.Topic> topicType,
                String topicId,
                String description,
                Collection<String> producerGroupIds,
                Collection<String> consumerGroupIds,
                Collection<String> connectorIds) {

            super(topicType, topicId, description);
            this.producerGroupIds.addAll(producerGroupIds);
            this.consumerGroupIds.addAll(consumerGroupIds);
            this.connectorIds.addAll(connectorIds);
        }

        public Set<String> getProducerGroupIds() {
            return producerGroupIds;
        }

        public Set<String> getConsumerGroupIds() {
            return consumerGroupIds;
        }

        public Set<String> getConnectorIds() {
            return connectorIds;
        }
    }
}
