package amp.topology.global;

import amp.topology.global.impl.BasePartition;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public interface Connector<PPART extends BasePartition, CPART extends BasePartition> extends TopologyItem {
    void setConnectorId(String id);

    String getConnectorId();

    void setProducerGroup(ProducerGroup<PPART> producerGroup);

    ProducerGroup getProducerGroup();

    void setConsumerGroup(ConsumerGroup<CPART> consumerGroup);

    ConsumerGroup getConsumerGroup();

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
     * Describes the potential states a connector can exist in.
     */
    public enum ConnectorStates {
        /**
         * The BaseConnector has never been setup or activated.
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
         * whether a BaseConnector is "PARTIALLY_ACTIVE" or not is up to the implementation.  Some implementations may
         * support a scenario where Partitions on the Producing side are provisioned, but remain INACTIVE in case of
         * a spike in traffic (etc.).
         */
        PARTIALLY_ACTIVE,
        /**
         * The BaseConnector is active (i.e. working).
         */
        ACTIVE,
        /**
         * The BaseConnector is the process of deactivating.
         */
        DEACTIVATING,
        /**
         * The BaseConnector is inactive (not in error, but turned off for some reason).
         */
        INACTIVE
    }
}
