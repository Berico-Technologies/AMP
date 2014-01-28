package amp.topology.global;

import amp.topology.global.lifecycle.LifeCycleObservationManager;
import com.yammer.metrics.annotation.Timed;

import java.util.Map;
import java.util.UUID;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public abstract class Connector<PPART extends Partition, CPART extends Partition> {

    private String id;

    private String description = "";

    private ConnectorStates connectorState = ConnectorStates.NONEXISTENT;

    private ProducerGroup<PPART> producerGroup;

    private ConsumerGroup<CPART> consumerGroup;

    public Connector(String description, ProducerGroup<PPART> producerGroup, ConsumerGroup<CPART> consumerGroup) {

        this(UUID.randomUUID().toString(), description, producerGroup, consumerGroup);
    }

    public Connector(String id, String description, ProducerGroup<PPART> producerGroup, ConsumerGroup<CPART> consumerGroup) {
        this.id = id;
        this.description = description;
        this.producerGroup = producerGroup;
        this.consumerGroup = consumerGroup;
    }

    /**
     * Set the Id of the Connector.
     * @param id Id of the Connector.
     */
    protected void setId(String id) {

        this.id = id;
    }

    /**
     * Get the Id of the Connector.
     * @return
     */
    public String getId() {

        return this.id;
    }

    /**
     * Set a friendly description of this Connector.
     * @param description Friendly description.
     */
    public void setDescription(String description) {

        this.description = description;
    }

    /**
     * Get a friendly description of this connector.
     * @return Friendly description.
     */
    public String getDescription() {

        return this.description;
    }

    /**
     * Set the ProducerGroup.
     * @param producerGroup ProducerGroup.
     */
    public void setProducerGroup(ProducerGroup<PPART> producerGroup) {

        this.producerGroup = producerGroup;
    }

    /**
     * Get the ProducerGroup for this Connector
     * @return ProducerGroup
     */
    public ProducerGroup getProducerGroup() {

        return this.producerGroup;
    }

    /**
     * Set the ConsumerGroup.
     * @param consumerGroup ConsumerGroup.
     */
    public void setConsumerGroup(ConsumerGroup<CPART> consumerGroup) {

        this.consumerGroup = consumerGroup;
    }

    /**
     * Get the ConsumerGroup for this Connector
     * @return ConsumerGroup
     */
    public ConsumerGroup getConsumerGroup() {

        return this.consumerGroup;
    }

    /**
     * Set the state of the Connector.  This is how derived classes set the Connector state
     * and notified listeners.
     * @param newState New Connector State
     */
    @Timed
    protected void setState(ConnectorStates newState, String reasonForChange) {

        ConnectorStates oldState = this.connectorState;

        this.connectorState = newState;

        LifeCycleObservationManager.fireOnStateChanged(this, oldState, newState, reasonForChange);
    }

    /**
     * Get the latest state of the Connector.
     * @return State of the Connector.
     */
    public ConnectorStates getState() {

        return this.connectorState;
    }

    /**
     * Verify the connector state.  If the state is invalid, throw an exception.
     *
     * It's expected that the state of the connector be updated during the verification.
     *
     * @throws Exception An exception that may have arisen while verifying the infrastructure is
     * in the correct state.
     */
    public abstract void verify() throws Exception;

    /**
     * Activate the Connector.  An implementation may provision resources, configure a broker, etc. (whatever
     * it needs to do to ensure the connection is in a valid state).
     * @throws Exception An exception propagated from the underlying implementation.
     */
    public abstract void activate() throws Exception;

    /**
     * Deactive the Connector.  Ensure that messages are not being transmitted between the ProducerGroup and
     * the ConsumerGroup.
     * @throws Exception An error encountered in the process of trying to stop the stream of messages.
     */
    public abstract void deactivate() throws Exception;

    /**
     * Called when the Connector is instantiated.  This is an opportunity for the connector to provision any resources
     * it may need to function before becoming active.
     * @throws Exception An error encountered during the setup process.
     */
    public abstract void setup() throws Exception;

    /**
     * Called when the Connector is being removed.  The Connector is given a chance to cleanup any configuration
     * it has left on the system.
     * @throws Exception An error encountered during the cleanup process.
     */
    public abstract void cleanup() throws Exception;

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
     * Mandatory state properties for a Connector.
     */
    public static class HydratedState extends TopologyState {

        private String connectorId;

        private String producerGroupId;

        private String consumerGroupId;

        public HydratedState(
                Class<? extends Connector> connectorType,
                String topicId,
                String connectorId,
                String description,
                String producerGroupId,
                String consumerGroupId,
                Map<String, String> extensionProperties) {

            super(connectorType, topicId, description);

            this.connectorId = connectorId;
            this.producerGroupId = producerGroupId;
            this.consumerGroupId = consumerGroupId;
        }

        public String getConnectorId() {
            return connectorId;
        }

        public String getProducerGroupId() {
            return producerGroupId;
        }

        public String getConsumerGroupId() {
            return consumerGroupId;
        }
    }
}
