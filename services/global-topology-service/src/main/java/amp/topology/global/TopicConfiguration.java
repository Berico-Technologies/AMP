package amp.topology.global;

import amp.topology.anubis.AccessControlList;
import amp.topology.global.exceptions.ConnectorNotExistException;
import amp.topology.global.exceptions.TopologyGroupNotExistException;
import amp.topology.global.filtering.RouteFilterResults;
import amp.topology.global.filtering.RouteRequirements;

import java.util.Collection;

/**
 * Container for the routes associated with a particular topology.
 *
 * The TopicConfiguration is responsible for managing the life cycle of
 * TopologyGroups and Connectors.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public interface TopicConfiguration {

    /**
     * A globally unique id amongst the set of topics.  This is the reference key used to look up
     * topics requested by clients.  Therefore, the key should be descriptive (like the canonical class
     * name of the event).
     *
     * If you want to support "aliases", resolution of the alias to the actual id is up to you.
     *
     * @return The id of the TopologyConfiguration.
     */
    String getId();

    /**
     * A friendly description describing this Topic.
     *
     * @return a friendly description.
     */
    String getDescription();

    /**
     * Get the Access Controls for this Topic Configuration class.
     * @return AccessControlList.
     */
    AccessControlList getACL();

    ///// Query+Filtering /////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Provide the applicable Topology Groups and Connectors that match the route requirements
     * of the requester.
     *
     * @param requirements Requirements of the client for a particular route.
     * @return The set of Groups and Connectors the client will need to bind to.
     */
    RouteFilterResults filter(RouteRequirements requirements);

    ///// Life Cycle  /////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Called when the Group is instantiated.  This is an opportunity for the Group to do whatever it needs to do
     * (like create an initial partition) in order to become ready.
     * @throws Exception An error encountered during the setup process.
     */
    void setup() throws Exception;

    /**
     * Called when the Group is being removed.  The Group is given a chance to shutdown and clean up partitions
     * it may have provisioned during the course of its life.
     * @throws Exception An error encountered during the cleanup process.
     */
    void cleanup() throws Exception;

    ///// CRUD for Producer Groups ////////////////////////////////////////////////////////////////////////////////////

    /**
     * Add a Producer Group (will call "setup" on the group), and fails to add the Group if there is a
     * problem setting up the group.
     * @param producerGroup
     * @throws Exception Thrown if there is a problem setting up the Group.
     */
    void addProducerGroup(ProducerGroup<? extends  Partition> producerGroup) throws Exception;

    /**
     * Remove a Producer Group by Id
     * @param id
     * @throws Exception
     */
    void removeProducerGroup(String id) throws Exception;

    /**
     * Get a Producer Group by Id
     * @param id
     * @return
     * @throws TopologyGroupNotExistException
     */
    ProducerGroup<? extends Partition> getProducerGroup(String id) throws TopologyGroupNotExistException;

    /**
     * Get all Producer Groups
     * @return
     */
    Collection<ProducerGroup<? extends Partition>> getProducerGroups();


    ///// CRUD for Consumer Groups ////////////////////////////////////////////////////////////////////////////////////

    /**
     * Add a Consumer Group (will call "setup" on the group), and fails to add the Group if there is a
     * problem setting up the group.
     * @param consumerGroup
     * @throws Exception Thrown if there is a problem setting up the Group.
     */
    void addConsumerGroup(ConsumerGroup<? extends Partition> consumerGroup) throws Exception;

    /**
     * Remove a Consumer Group by Id
     * @param id
     * @throws Exception
     */
    void removeConsumerGroup(String id) throws Exception;

    /**
     * Get a Consumer Group by Id
     * @param id
     * @return
     * @throws TopologyGroupNotExistException
     */
    ConsumerGroup<? extends Partition> getConsumerGroup(String id) throws TopologyGroupNotExistException;

    /**
     * Get all Consumer Groups
     * @return
     */
    Collection<ConsumerGroup<? extends Partition>> getConsumerGroups();


    ///// CRUD for Connectors /////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Add a Connector (will call "setup" on the connector), and fails to add the Connector if there is a
     * problem in the setup process.
     * @param connector
     */
    void addConnector(Connector<? extends Partition, ? extends Partition> connector) throws Exception;

    /**
     * Remove a Connector
     * @param id
     * @throws Exception
     */
    void removeConnector(String id) throws Exception;

    /**
     * Get a Connector by Id
     * @param id
     * @return
     * @throws ConnectorNotExistException
     */
    Connector<? extends Partition, ? extends Partition> getConnector(String id) throws ConnectorNotExistException;

    /**
     * Get all Connectors
     * @return
     */
    Collection<Connector<? extends Partition, ? extends Partition>> getConnectors();

    ///// Listeners ///////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Add a listener.
     * @param listener
     */
    void addListener(Listener listener);

    /**
     * Remove a listener.
     * @param listener
     */
    void removeListener(Listener listener);

    /**
     * Listener for Topology Configuration changes.
     */
    public interface Listener {

        /**
         * Called when a ProducerGroup is added.
         * @param producerGroup
         */
        void onProducerGroupAdded(ProducerGroup<? extends Partition> producerGroup);

        /**
         * Called when a ProducerGroup is removed.
         * @param producerGroup
         */
        void onProducerGroupRemoved(ProducerGroup<? extends Partition> producerGroup);

        /**
         * Called when a ConsumerGroup is added.
         * @param consumerGroup
         */
        void onConsumerGroupAdded(ConsumerGroup<? extends Partition> consumerGroup);

        /**
         * Called when a ConsumerGroup is removed.
         * @param consumerGroup
         */
        void onConsumerGroupRemoved(ConsumerGroup<? extends Partition> consumerGroup);

        /**
         * Called when a Connector is added.
         * @param connector
         */
        void onConnectorAdded(Connector<? extends Partition, ? extends Partition> connector);

        /**
         * Called when a Connector is removed.
         * @param connector
         */
        void onConnectorRemoved(Connector<? extends Partition, ? extends Partition> connector);
    }
}
