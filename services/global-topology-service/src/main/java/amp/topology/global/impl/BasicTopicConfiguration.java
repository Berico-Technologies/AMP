package amp.topology.global.impl;

import amp.topology.anubis.AccessControlList;
import amp.topology.global.*;
import amp.topology.global.exceptions.ConnectorAlreadyExistsException;
import amp.topology.global.exceptions.ConnectorNotExistException;
import amp.topology.global.exceptions.TopologyGroupAlreadyExistsException;import amp.topology.global.exceptions.TopologyGroupNotExistException;
import amp.topology.global.filtering.RouteFilterResults;
import amp.topology.global.filtering.RouteRequirements;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * A Basic implementation of a Topic Configuration that should suit most needs.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class BasicTopicConfiguration implements TopicConfiguration {

    private String id;

    private String description;

    private AccessControlList acl;

    private Object producerGroupsLock = new Object();

    private Set<ProducerGroup<?>> producerGroups = Sets.newCopyOnWriteArraySet();

    private Object consumerGroupsLock = new Object();

    private Set<ConsumerGroup<?>> consumerGroups = Sets.newCopyOnWriteArraySet();

    private Object connectorsLock = new Object();

    private Set<Connector<?, ?>> connectors = Sets.newCopyOnWriteArraySet();

    private Set<Listener> listeners = Sets.newCopyOnWriteArraySet();

    /**
     * Instantiate the Topic with it's id.
     * @param id A globally unique id in the topic space.  Typically this is the name of an event (canonical class name)
     *           or some easily identified but more generic category (e.g. "user-queues").
     */
    public BasicTopicConfiguration(String id){

        this.id = id;
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
    @Override
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
    @Override
    public String getDescription() {

        return this.description;
    }

    /**
     * Get the Access Control List for this Topic.
     * @return Access Control List.
     */
    @Override
    public AccessControlList getACL() {

        return this.acl;
    }

    /**
     * Filter the Topology entities based on the supplied Route Requirements.
     * @param requirements Requirements of the client for a particular route.
     * @return Filtered Route Results.
     */
    @Override
    public RouteFilterResults filter(RouteRequirements requirements) {

        RouteFilterResults.Builder resultsBuilder = RouteFilterResults.Builder(requirements);

        for (ProducerGroup<?> pgroup : producerGroups){

            Collection<? extends Partition> applicablePartitions = pgroup.filter(requirements);

            resultsBuilder.produceOn(applicablePartitions.toArray(new Partition[]{}));
        }

        for (ConsumerGroup<?> cgroup : consumerGroups){

            Collection<? extends Partition> applicablePartitions = cgroup.filter(requirements);

            resultsBuilder.consumeOn(applicablePartitions.toArray(new Partition[]{}));
        }

        return resultsBuilder.build();
    }

    /**
     * Does nothing.
     * @throws Exception Nope.
     */
    @Override
    public void setup() throws Exception {}

    /**
     * Ensures all groups and connectors are properly cleaned up.
     * @throws Exception Probably an exception occurring during the cleanup process of a Group, Connector, or Partition.
     */
    @Override
    public void cleanup() throws Exception {

        // First, stop producers from producing!
        for (ProducerGroup<?> producerGroup : producerGroups)
            producerGroup.cleanup();

        producerGroups.clear();

        // Second, unbind the connections
        for (Connector<?, ?> connector : connectors)
            connector.cleanup();

        connectors.clear();

        // Finally, tell the consumers to stop.
        for (ConsumerGroup<?> consumerGroup : consumerGroups)
            consumerGroup.cleanup();

        consumerGroups.clear();
    }

    /**
     * Add a ProducerGroup to the TopicConfiguration.  The "setup" method will be called on the group,
     * and if an exception is raised, the group will fail to be added to the Topic.  All listeners will
     * also be fired for onProducerGroupAdded.
     * @param producerGroup ProducerGroup to add.
     * @throws Exception Setup error.
     * @throws TopologyGroupAlreadyExistsException if the group already exists, an exception will be raised.
     */
    @Override
    public void addProducerGroup(ProducerGroup<? extends Partition> producerGroup) throws Exception {

        synchronized (producerGroupsLock) {

            if (!pgroupContains(producerGroups, producerGroup)){

                producerGroup.setup();

                producerGroups.add(producerGroup);

                for (Listener listener : listeners)
                    if (listener != null) listener.onProducerGroupAdded(producerGroup);
            }
            else {

                throw new TopologyGroupAlreadyExistsException(this.getId(), producerGroup.getId(), true);
            }
        }
    }

    /**
     * Remove a ProducerGroup.  This will call the "cleanup" method on the group, and if an exception is raised,
     * will fail to remove the group.  All listeners will also be fired for onProducerGroupRemoved.
     * @param id Id of the Group to remove.
     * @throws Exception raised during "cleanup"
     * @throws TopologyGroupNotExistException if the group doesn't exist
     */
    @Override
    public void removeProducerGroup(String id) throws Exception {

        synchronized (producerGroupsLock) {

            if (pgroupContains(producerGroups, id)){

                ProducerGroup<?> producerGroup = this.getProducerGroup(id);

                producerGroup.cleanup();

                producerGroups.remove(producerGroup);

                for (Listener listener : listeners)
                    if (listener != null) listener.onProducerGroupRemoved(producerGroup);
            }
            else {

                throw new TopologyGroupNotExistException(this.getId(), id, true);
            }
        }
    }

    /**
     * Get a ProducerGroup by it's id.
     * @param id Id of the ProducerGroup
     * @return A ProducerGroup
     * @throws TopologyGroupNotExistException if a group does not exist with that id.
     */
    @Override
    public ProducerGroup<? extends Partition> getProducerGroup(String id) throws TopologyGroupNotExistException {

        for(ProducerGroup<? extends Partition> pgroup : producerGroups)
            if (pgroup.getId().equals(id)) return pgroup;

        throw new TopologyGroupNotExistException(this.getId(), id, true);
    }

    /**
     * Get a unmodifiable collection of the ProducerGroups.
     * @return unmodifiable collection of the ProducerGroups
     */
    @Override
    public Collection<ProducerGroup<? extends Partition>> getProducerGroups() {

        return (Collection<ProducerGroup<? extends Partition>>)Collections.unmodifiableCollection(producerGroups);
    }

    /**
     * Add a ConsumerGroup to the TopicConfiguration.  The "setup" method will be called on the group,
     * and if an exception is raised, the group will fail to be added to the Topic.  All listeners will
     * also be fired for onConsumerGroupAdded.
     * @param consumerGroup ConsumerGroup to add.
     * @throws Exception Setup error.
     * @throws TopologyGroupAlreadyExistsException if the group already exists, an exception will be raised.
     */
    @Override
    public void addConsumerGroup(ConsumerGroup<? extends Partition> consumerGroup) throws Exception {

        synchronized (consumerGroupsLock) {

            if (!cgroupContains(consumerGroups, consumerGroup)){

                consumerGroup.setup();

                consumerGroups.add(consumerGroup);

                for (Listener listener : listeners)
                    if (listener != null) listener.onConsumerGroupAdded(consumerGroup);
            }
            else {

                throw new TopologyGroupAlreadyExistsException(this.getId(), consumerGroup.getId(), true);
            }
        }
    }

    /**
     * Remove a ConsumerGroup.  This will call the "cleanup" method on the group, and if an exception is raised,
     * will fail to remove the group.  All listeners will also be fired for onConsumerGroupRemoved.
     * @param id Id of the Group to remove.
     * @throws Exception raised during "cleanup"
     * @throws TopologyGroupNotExistException if the group doesn't exist
     */
    @Override
    public void removeConsumerGroup(String id) throws Exception {

        synchronized (consumerGroupsLock) {

            if (cgroupContains(consumerGroups, id)){

                ConsumerGroup<?> consumerGroup = this.getConsumerGroup(id);

                consumerGroup.cleanup();

                consumerGroups.remove(consumerGroup);

                for (Listener listener : listeners)
                    if (listener != null) listener.onConsumerGroupRemoved(consumerGroup);
            }
            else {

                throw new TopologyGroupNotExistException(this.getId(), id, false);
            }
        }
    }

    /**
     * Get a ConsumerGroup by it's id.
     * @param id Id of the ConsumerGroup.
     * @return A ConsumerGroup with the supplied Id.
     * @throws TopologyGroupNotExistException Thrown if a Group with that Id does not exist.
     */
    @Override
    public ConsumerGroup<? extends Partition> getConsumerGroup(String id) throws TopologyGroupNotExistException {

        for(ConsumerGroup<? extends Partition> cgroup : consumerGroups)
            if (cgroup.getId().equals(id)) return cgroup;

        throw new TopologyGroupNotExistException(this.getId(), id, false);
    }

    /**
     * Get an unmodifiable collection of the ConsumerGroups.
     * @return unmodifiable collection of the ConsumerGroups.
     */
    @Override
    public Collection<ConsumerGroup<? extends Partition>> getConsumerGroups() {

        return (Collection<ConsumerGroup<? extends Partition>>)Collections.unmodifiableCollection(consumerGroups);
    }

    /**
     * Add a Connector to the Topic.  This will call "setup" on the connector, which may error.  In the event
     * of an error, the connector will not be added.  Listeners for onConnectorAdded will also be called.
     * @param connector Connector to add.
     * @throws Exception If an error occurs during "setup"
     * @throws ConnectorAlreadyExistsException if the connector already exists.
     */
    @Override
    public void addConnector(Connector<? extends Partition, ? extends Partition> connector) throws Exception {

        synchronized (connectorsLock) {

            if (!connectorsContains(connectors, connector)){

                connector.setup();

                connectors.add(connector);

                for (Listener listener : listeners)
                    if (listener != null) listener.onConnectorAdded(connector);
            }
            else {

                throw new ConnectorAlreadyExistsException(this.getId(), connector.getId());
            }
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

        synchronized (connectorsLock) {

            if (connectorsContains(connectors, id)){

                Connector<?, ?> connector = this.getConnector(id);

                connector.cleanup();

                connectors.remove(connector);

                for (Listener listener : listeners)
                    if (listener != null) listener.onConnectorRemoved(connector);
            }
            else {

                throw new ConnectorNotExistException(this.getId(), id);
            }
        }
    }

    /**
     * Get a Connector by it's id.
     * @param id Id of the Connector.
     * @return Connector
     * @throws ConnectorNotExistException if a connector with the specified Id does not exist.
     */
    @Override
    public Connector<? extends Partition, ? extends Partition> getConnector(String id)
            throws ConnectorNotExistException {

        for(Connector<? extends Partition, ? extends Partition> connector : connectors)
            if (connector.getId().equals(id)) return connector;

        throw new ConnectorNotExistException(this.getId(), id);
    }

    /**
     * Returns an unmodifiable collection of Connectors managed by this TopicConfiguration instance.
     * @return unmodifiable collection of Connectors.
     */
    @Override
    public Collection<Connector<? extends Partition, ? extends Partition>> getConnectors() {

        return (Collection<Connector<? extends Partition, ? extends Partition>>)
                Collections.unmodifiableCollection(connectors);
    }

    /**
     * Add a TopicConfiguration Listener.  Nulls and already registered listeners are ignored.
     * @param listener
     */
    @Override
    public void addListener(Listener listener) {

        if (listener != null && !listeners.contains(listener)) listeners.add(listener);
    }

    /**
     * Remove a TopicConfiguration listener.  Nulls and non-registered listeners are ignored.
     * @param listener
     */
    @Override
    public void removeListener(Listener listener) {

        if (listener != null && listeners.contains(listener)) listeners.remove(listener);
    }

    /**
     * Does the groups collection contain the target group?  We do this to ensure groups carry unique
     * id's before inserting them (or to find a group by id).
     * @param groups Groups collection to search
     * @param target Group to determine if it's in the collection.
     * @return TRUE if the TARGET group is in the GROUPS collection.
     */
    static boolean pgroupContains(Set<ProducerGroup<?>> groups, ProducerGroup<?> target){

        // Short circuit the evaluation if there is an identity-based match.
        if (groups.contains(target)) return true;

        return pgroupContains(groups, target.getId());
    }

    /**
     * Does the groups collection contain a group with the supplied id?
     * @param groups Groups to search
     * @param id ID of the group to find
     * @return TRUE if it does contain the group.
     */
    static boolean pgroupContains(Set<ProducerGroup<?>> groups, String id){

        for (ProducerGroup<?> group : groups){

            if (group.getId().equals(id)) return true;
        }
        return false;
    }

    /**
     * Does the groups collection contain the target group?  We do this to ensure groups carry unique
     * id's before inserting them (or to find a group by id).
     * @param groups Groups collection to search
     * @param target Group to determine if it's in the collection.
     * @return TRUE if the TARGET group is in the GROUPS collection.
     */
    static boolean cgroupContains(Set<ConsumerGroup<?>> groups, ConsumerGroup<?> target){

        // Short circuit the evaluation if there is an identity-based match.
        if (groups.contains(target)) return true;

        return cgroupContains(groups, target.getId());
    }

    /**
     * Does the groups collection contain a group with the supplied id?
     * @param groups Groups to search
     * @param id ID of the group to find
     * @return TRUE if it does contain the group.
     */
    static boolean cgroupContains(Set<ConsumerGroup<?>> groups, String id){

        for (ConsumerGroup<?> group : groups){

            if (group.getId().equals(id)) return true;
        }
        return false;
    }

    /**
     * Does the connectors collection contain the target connector?  We do this to ensure groups carry unique
     * id's before inserting them (or to find a group by id).
     * @param connectors Connectors collection to search
     * @param target Connector to determine if it's in the collection.
     * @return TRUE if the TARGET group is in the CONNECTORS collection.
     */
    static boolean connectorsContains(Set<Connector<?, ?>> connectors, Connector<?, ?> target){

        // Short circuit the evaluation if there is an identity-based match.
        if (connectors.contains(target)) return true;

        return connectorsContains(connectors, target.getId());
    }

    /**
     * Does the connector collection contain a connector with the supplied id?
     * @param connectors Connectors to search
     * @param id ID of the connector to find
     * @return TRUE if it does contain the connector.
     */
    static boolean connectorsContains(Set<Connector<?, ?>> connectors, String id){

        for (Connector<?, ?> connector : connectors){

            if (connector.getId().equals(id)) return true;
        }
        return false;
    }
}
