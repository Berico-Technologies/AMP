package amp.topology.global;

/**
 * Represents a connection between a ProducerGroup and a ConsumerGroup.  This maybe a logical connection,
 * configuration (say a routing key for an Exchange + Queue binging in AMQP), or a complex bridge
 * (protocol transition).
 *
 * Note: this construct is meant to be transparent to clients consuming Routes.  It serves more as a management
 * feature to configure pairs of Producer and Consumer Groups to carry the correct information about how to
 * connect (like RoutingKeys in AMQP), or to setup infrastructure to perform the bridging.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public interface Connector<PRODUCING_PARTITION extends Partition, CONSUMING_PARTITION extends Partition> {

    /**
     * A unique identifier for the Connector.  Generally, this only needs to be unique to the TopicSpace, but
     * it can be globally unique to suit your data storage strategy.
     * @return Id
     */
    String getId();

    /**
     * Some friendly description about this connector.
     * @return Description
     */
    String getDescription();

    /**
     * Set the description of the Connector.
     *
     * @param description Description of the connector.
     */
    void setDescription(String description);

    /**
     * The ProducerGroup that represents the Inflow of messages.
     * @return
     */
    ProducerGroup<PRODUCING_PARTITION> getProducerGroup();

    /**
     * The ConsumerGroup that represents the Outflow of messages.
     * @return
     */
    ConsumerGroup<CONSUMING_PARTITION> getConsumerGroup();

    /**
     * The latest state of the connector.
     * @return Connector State.
     */
    ConnectorStates getState();

    /**
     * Verify the connector state.  If the state is invalid, throw an exception.
     *
     * It's expected that the state of the connector be updated during the verification.
     *
     * @throws Exception An exception that may have arisen while verifying the infrastructure is
     * in the correct state.
     */
    void verify() throws Exception;

    /**
     * Activate the Connector.  An implementation may provision resources, configure a broker, etc. (whatever
     * it needs to do to ensure the connection is in a valid state).
     * @throws Exception An exception propagated from the underlying implementation.
     */
    void activate() throws Exception;

    /**
     * Deactive the Connector.  Ensure that messages are not being transmitted between the ProducerGroup and
     * the ConsumerGroup.
     * @throws Exception An error encountered in the process of trying to stop the stream of messages.
     */
    void deactivate() throws Exception;

    /**
     * Called when the Connector is instantiated.  This is an opportunity for the connector to provision any resources
     * it may need to function before becoming active.
     * @throws Exception An error encountered during the setup process.
     */
    void setup() throws Exception;

    /**
     * Called when the Connector is being removed.  The Connector is given a chance to cleanup any configuration
     * it has left on the system.
     * @throws Exception An error encountered during the cleanup process.
     */
    void cleanup() throws Exception;

    /**
     * Describes the potential states a connector can exist in.
     */
    public enum ConnectorStates {
        /**
         * The Connector has never been setup or activated.
         */
        NONEXISTENT,
        /**
         * The state of the connector or it's underlying TopologyGroups are in error.
         */
        IN_ERROR,
        /**
         * The connector is in the process of becoming active.  This may include provisioning
         * resources on remote servers, etc.
         */
        ACTIVATING,
        /**
         * The connector is active (correctly configured or provisioned), but for some reason, cannot connect
         * all of the Partitions in the ProducerGroup with all of the Partitions in the ConsumerGroup.  This may
         * occur when Partitions on either side are IN_ERROR or in a state other than "ACTIVE".  The logic as to
         * whether a Connector is "PARTIALLY_ACTIVE" or not is up to the implementation.  Some implementations may
         * support a scenario where Partitions on the Producing side are provisioned, but remain INACTIVE in case of
         * a spike in traffic (etc.).
         */
        PARTIALLY_ACTIVE,
        /**
         * The Connector is active (i.e. working).
         */
        ACTIVE,
        /**
         * The Connector is the process of deactivating.
         */
        DEACTIVATING,
        /**
         * The Connector is inactive (not in error, but turned off for some reason).
         */
        INACTIVE
    }

    /**
     * Add a listener to the Connector
     * @param listener
     */
    void addListener(Listener listener);

    /**
     * Remove a listener from the Connector
     * @param listener
     */
    void removeListener(Listener listener);

    /**
     * Listener for life cycle events on the Connector.
     */
    public interface Listener {

        /**
         * Called when the Connector changes state.
         * @param thisConnector The Connector changing (usually this).
         * @param oldState The state the Connector is moving away from.
         * @param newState The state the Connector is moving to.
         * @param reasonForChange The reason why the Connector has changed state.
         */
        void onConnectorStateChange(
                Connector<? extends Partition, ? extends Partition> thisConnector,
                ConnectorStates oldState,
                ConnectorStates newState,
                String reasonForChange);
    }
}
