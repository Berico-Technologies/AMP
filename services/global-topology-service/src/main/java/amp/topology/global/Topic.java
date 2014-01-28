package amp.topology.global;

import amp.topology.global.exceptions.ConnectorAlreadyExistsException;
import amp.topology.global.exceptions.ConnectorNotExistException;
import amp.topology.global.exceptions.TopologyGroupAlreadyExistsException;
import amp.topology.global.exceptions.TopologyGroupNotExistException;
import amp.topology.global.filtering.RouteFilterResults;
import amp.topology.global.filtering.RouteRequirements;
import amp.topology.global.lifecycle.LifeCycleObservationManager;
import amp.topology.global.lifecycle.PersistenceManager;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

/**
 * Container for the routes associated with a particular topology.
 *
 * The Topic is responsible for managing the life cycle of
 * TopologyGroups and Connectors.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class Topic {

    private String id;

    private String description;

    private ConcurrentMap<String, ProducerGroup<?>> pgroups = Maps.newConcurrentMap();

    private ConcurrentMap<String, ConsumerGroup<?>> cgroups = Maps.newConcurrentMap();

    private ConcurrentMap<String, Connector<?, ?>> connectors = Maps.newConcurrentMap();

    /**
     * No! No!  Don't you dare!  This is for state hydration purposes only!
     */
    public Topic(){}

    /**
     * Instantiate the Topic with it's id.
     * @param id A globally unique id in the topic space.  Typically this is the name of an event (canonical class name)
     *           or some easily identified but more generic category (e.g. "user-queues").
     */
    public Topic(String id){

        this.id = id;
    }

    /**
     * Instantiate the Topic with it's id.
     * @param id A globally unique id in the topic space.  Typically this is the name of an event (canonical class name)
     *           or some easily identified but more generic category (e.g. "user-queues").
     * @param description A friendly description of this topic.
     */
    public Topic(String id, String description) {

        this.id = id;
        this.description = description;
    }

    /**
     * Set the Id of the Topic in a manner consistent with your storage implementation.
     * @param id Id of the Topic
     */
    protected void setId(String id) {

        this.id = id;
    }

    /**
     * Get the Id of the Topic.
     * @return Id of the Topic
     */
    public String getId() {

        return this.id;
    }

    /**
     * Set a friendly description of this Topic.
     * @param description Friendly description.
     */
    public void setDescription(String description) {

        this.description = description;
    }

    /**
     * A friendly description of the Topic.
     * @return Friendly description.
     */
    public String getDescription() {

        return this.description;
    }

    /**
     * Filter the Topology entities based on the supplied Route Requirements.
     * @param requirements Requirements of the client for a particular route.
     * @return Filtered Route Results.
     */
    public RouteFilterResults filter(RouteRequirements requirements) {

        RouteFilterResults.Builder resultsBuilder = RouteFilterResults.Builder(requirements);

        for (ProducerGroup<?> pgroup : pgroups.values()){

            Collection<? extends Partition> applicablePartitions = pgroup.filter(requirements);

            resultsBuilder.produceOn(applicablePartitions.toArray(new Partition[]{}));
        }

        for (ConsumerGroup<?> cgroup : cgroups.values()){

            Collection<? extends Partition> applicablePartitions = cgroup.filter(requirements);

            resultsBuilder.consumeOn(applicablePartitions.toArray(new Partition[]{}));
        }

        return resultsBuilder.build();
    }

    /**
     * Does nothing.
     * @throws Exception Nope.
     */
    public void setup() throws Exception {}

    /**
     * Ensures all groups and connectors are properly cleaned up.
     * @throws Exception Probably an exception occurring during the cleanup process of a Group, Connector, or Partition.
     */
    public void cleanup() throws Exception {

        // First, stop producers from producing!
        for (ProducerGroup<?> producerGroup : pgroups.values())
            producerGroup.cleanup();

        pgroups.clear();

        // Second, unbind the connections
        for (Connector<?, ?> connector : connectors.values())
            connector.cleanup();

        connectors.clear();

        // Finally, tell the consumers to stop.
        for (ConsumerGroup<?> consumerGroup : cgroups.values())
            consumerGroup.cleanup();

        cgroups.clear();
    }

    public void save(){

        HydratedState topicState =
                new HydratedState(
                        getClass(),
                        this.getId(),
                        this.getDescription(),
                        pgroups.keySet(),
                        cgroups.keySet(),
                        connectors.keySet());

        PersistenceManager.topics().save(topicState);
    }

    public void restore(HydratedState state){

        setId(state.getTopicId());

        setDescription(state.getDescription());

        restoreFromProperties(state.getExtensionProperties());

        // That's it.  Group and Connector dehydration will occur outside of this implementation.
    }

    /**
     * Override me!
     * @param properties
     */
    public void restoreFromProperties(Map<String, String> properties){}

    /**
     * Get a TopologyGroup by it's id; it doesn't matter whether that is a producer or consumer group.
     * @param id ID of the Group to retrieve.
     * @return TopologyGroup
     * @throws Exception Thrown if the group does not exist.
     */
    public TopologyGroup<? extends Partition> getGroup(String id) throws Exception {

        GroupExists groupExists = getGroupExists(id);

        if (groupExists == GroupExists.AsProducer)
            return getProducerGroup(id);

        else if (groupExists == GroupExists.AsConsumer)
            return getConsumerGroup(id);

        else
            throw new TopologyGroupNotExistException(this.getId(), id, false);
    }

    /**
     * Remove a Group by it's id; it doesn't matter whether the group is a producer or consumer group.
     * @param id ID of the Group to remove.
     * @throws Exception Thrown if the group doesn't exist, or an error is encountered cleanup resources of that group.
     */
    public void removeGroup(String id) throws Exception {

        GroupExists groupExists = getGroupExists(id);

        if (groupExists == GroupExists.AsProducer)
            removeProducerGroup(id);

        else if (groupExists == GroupExists.AsConsumer)
            removeConsumerGroup(id);

        else
            throw new TopologyGroupNotExistException(this.getId(), id, false);
    }

    /**
     * Does the group with the specified id exist?
     * @param id ID of the group to check existence for.
     * @return TRUE if it does exist, FALSE if it does not.
     */
    public boolean groupExists(String id) {

        GroupExists groupExists = getGroupExists(id);

        return groupExists != GroupExists.False;
    }

    /**
     * Specifies the state of a group's existence.
     */
    public enum GroupExists {
        AsProducer,
        AsConsumer,
        False
    }

    /**
     * A more complex form of groupExists(id), letting you know whether it's a Producer, Consumer, or Nonexistent.
     * @param id Id of the Group
     * @return Status of Existence.
     */
    public GroupExists getGroupExists(String id){

        if (pgroups.containsKey(id)) return GroupExists.AsProducer;

        if (cgroups.containsKey(id)) return GroupExists.AsConsumer;

        return GroupExists.False;
    }

    /**
     * Add a ProducerGroup to the TopicConfiguration.  The "setup" method will be called on the group,
     * and if an exception is raised, the group will fail to be added to the Topic.  All listeners will
     * also be fired for onProducerGroupAdded.
     * @param producerGroup ProducerGroup to add.
     * @throws Exception Setup error.
     * @throws TopologyGroupAlreadyExistsException if the group already exists, an exception will be raised.
     */
    public void addProducerGroup(ProducerGroup<? extends Partition> producerGroup) throws Exception {

        if (null == pgroups.putIfAbsent(producerGroup.getId(), producerGroup)){

            producerGroup.setup();

            LifeCycleObservationManager.fireOnAdded(producerGroup);
        }
        else {
            throw new TopologyGroupAlreadyExistsException(this.getId(), producerGroup.getId(), true);
        }
    }

    /**
     * Remove a ProducerGroup.  This will call the "cleanup" method on the group, and if an exception is raised,
     * will fail to remove the group.  All listeners will also be fired for onProducerGroupRemoved.
     * @param id Id of the Group to remove.
     * @throws Exception raised during "cleanup"
     * @throws TopologyGroupNotExistException if the group doesn't exist
     */
     void removeProducerGroup(String id) throws Exception {

         ProducerGroup<?> group = pgroups.remove(id);

         if (group != null){

             group.cleanup();

             LifeCycleObservationManager.fireOnRemoved(group);
         }
         else {

             throw new TopologyGroupNotExistException(this.getId(), id, true);
         }
    }

    /**
     * Get a ProducerGroup by it's id.
     * @param id Id of the ProducerGroup
     * @return A ProducerGroup
     * @throws TopologyGroupNotExistException if a group does not exist with that id.
     */
    public ProducerGroup<? extends Partition> getProducerGroup(String id) throws TopologyGroupNotExistException {

        ProducerGroup<?> group = pgroups.get(id);

        if (group == null) throw new TopologyGroupNotExistException(this.getId(), id, true);

        return group;
    }

    /**
     * Get a unmodifiable collection of the ProducerGroups.
     * @return unmodifiable collection of the ProducerGroups
     */
    public Collection<ProducerGroup<? extends Partition>> getProducerGroups() {

        return (Collection<ProducerGroup<? extends Partition>>)pgroups.values();
    }

    /**
     * Add a ConsumerGroup to the TopicConfiguration.  The "setup" method will be called on the group,
     * and if an exception is raised, the group will fail to be added to the Topic.  All listeners will
     * also be fired for onConsumerGroupAdded.
     * @param consumerGroup ConsumerGroup to add.
     * @throws Exception Setup error.
     * @throws TopologyGroupAlreadyExistsException if the group already exists, an exception will be raised.
     */
    public void addConsumerGroup(ConsumerGroup<? extends Partition> consumerGroup) throws Exception {

        if (null == cgroups.putIfAbsent(consumerGroup.getId(), consumerGroup)){

            consumerGroup.setup();

            LifeCycleObservationManager.fireOnAdded(consumerGroup);
        }
        else {

            throw new TopologyGroupAlreadyExistsException(this.getId(), consumerGroup.getId(), true);
        }
    }

    /**
     * Remove a ConsumerGroup.  This will call the "cleanup" method on the group, and if an exception is raised,
     * will fail to remove the group.  All listeners will also be fired for onConsumerGroupRemoved.
     * @param id Id of the Group to remove.
     * @throws Exception raised during "cleanup"
     * @throws TopologyGroupNotExistException if the group doesn't exist
     */
    void removeConsumerGroup(String id) throws Exception {

        ConsumerGroup<?> group = cgroups.remove(id);

        if (group != null){

            group.cleanup();

            LifeCycleObservationManager.fireOnRemoved(group);
        }
        else {

            throw new TopologyGroupNotExistException(this.getId(), id, true);
        }
    }

    /**
     * Get a ConsumerGroup by it's id.
     * @param id Id of the ConsumerGroup.
     * @return A ConsumerGroup with the supplied Id.
     * @throws TopologyGroupNotExistException Thrown if a Group with that Id does not exist.
     */
    public ConsumerGroup<? extends Partition> getConsumerGroup(String id) throws TopologyGroupNotExistException {

        ConsumerGroup<?> group = cgroups.get(id);

        if (group == null) throw new TopologyGroupNotExistException(this.getId(), id, true);

        return group;
    }

    /**
     * Get an unmodifiable collection of the ConsumerGroups.
     * @return unmodifiable collection of the ConsumerGroups.
     */
    public Collection<ConsumerGroup<? extends Partition>> getConsumerGroups() {

        return (Collection<ConsumerGroup<? extends Partition>>)cgroups.values();
    }

    /**
     * Add a Connector to the Topic.  This will call "setup" on the connector, which may error.  In the event
     * of an error, the connector will not be added.  Listeners for onConnectorAdded will also be called.
     * @param connector Connector to add.
     * @throws Exception If an error occurs during "setup"
     * @throws ConnectorAlreadyExistsException if the connector already exists.
     */
    public void addConnector(Connector<? extends Partition, ? extends Partition> connector) throws Exception {

        if (null == connectors.putIfAbsent(connector.getId(), connector)){

            connector.setup();

            LifeCycleObservationManager.fireOnAdded(connector);
        }
        else {

            throw new ConnectorAlreadyExistsException(this.getId(), connector.getId());
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
    public void removeConnector(String id) throws Exception {

        Connector<?, ?> connector = connectors.remove(id);

        if (connector != null){

            connector.cleanup();

            LifeCycleObservationManager.fireOnRemoved(connector);
        }
        else {

            throw new ConnectorNotExistException(this.getId(), id);
        }
    }

    /**
     * Get a Connector by it's id.
     * @param id Id of the Connector.
     * @return Connector
     * @throws ConnectorNotExistException if a connector with the specified Id does not exist.
     */
    public Connector<? extends Partition, ? extends Partition> getConnector(String id)
            throws ConnectorNotExistException {

        Connector<?, ?> connector = connectors.get(id);

        if (connector == null) throw new ConnectorNotExistException(this.getId(), id);

        return connector;
    }

    /**
     * Returns an unmodifiable collection of Connectors managed by this TopicConfiguration instance.
     * @return unmodifiable collection of Connectors.
     */
    public Collection<Connector<? extends Partition, ? extends Partition>> getConnectors() {

        return (Collection<Connector<? extends Partition, ? extends Partition>>)connectors.values();
    }

    /**
     * Represents the mandatory properties for a Topic.
     */
    public static class HydratedState extends TopologyState {

        private Set<String> producerGroupIds = Sets.newHashSet();

        private Set<String> consumerGroupIds = Sets.newHashSet();

        private Set<String> connectorIds = Sets.newHashSet();

        public HydratedState(
                Class<? extends Topic> topicType,
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
