package amp.topology.global;

/**
 * Partitions are a unit of scale for a TopologyGroup.  Partitions typically map one-to-one with a "route";
 * of course this depends largely on the protocol.  For AMQP, this may mean that the Partition represents
 * an Broker, Virtual Host, Exchange, and possibly a routing key and Queue (if consuming).
 *
 * @author Richard Clayton (Berico Technologies)
 */
public interface Partition {

    /**
     * The id of the partition.  This only needs to be unique to the Group in which it belongs, but
     * can optionally be globally unique depending on data store requirements.
     * @return
     */
    String getId();

    /**
     * Attempt to activate the partition.
     * @throws Exception could represent an error in attempting to achieve the active state,
     * like a misconfiguration or inability to provision resources.
     */
    void activate() throws Exception;

    /**
     * Deactive the partition.
     * @throws Exception could represent an error in attempting to achieve the inactive state,
     * like having active clients and not being able to cleanup them.
     */
    void deactive() throws Exception;

    /**
     * Called when the Partition is instantiated.  This is an opportunity for the Partition to provision any resources
     * it may need to function before becoming active.
     * @throws Exception An error encountered during the setup process.
     */
    void setup() throws Exception;

    /**
     * Called when the Partition is being removed.  The Partition is given a chance to cleanup any configuration
     * it has left on the system.
     * @throws Exception An error encountered during the cleanup process.
     */
    void cleanup() throws Exception;

    /**
     * Get the Current state of the partition.
     * @return Partition state.
     */
    PartitionStates getState();

    /**
     * The states a Partition can exist in.
     */
    public enum PartitionStates {
        /**
         * The Partition has never been setup or activated.
         */
        NONEXISTENT,
        /**
         * The partition is in error by some means, and will not participate
         * in topology queries.
         */
        IN_ERROR,
        /**
         * The partition is in the process of becoming active.
         */
        ACTIVATING,
        /**
         * The partition is active and accepting requests.
         */
        ACTIVE,
        /**
         * The partition is in the process of becoming inactive.
         */
        DEACTIVATING,
        /**
         * The partition is inactive and will not participate in topology queries.
         */
        INACTIVE
    }

    /**
     * Verify the state of the partition.  This is called to explicitly verify the state of the
     * partition.
     *
     * This method will generally be called by the GTS infrastructure on some schedule to ensure the
     * state of partitions are valid.  The verify check is not idempotent.  If the Partition State is
     * out of sync with the underlying infrastructure, it is expected that the verify method will
     * correct the valid state and fire event handlers (while indicating that the state was invalid = FALSE).
     *
     * @return If the latest state is correct, the method will return TRUE.  This means that if the
     * Partition State is in error, and verify() determines that it is in error, this method will return
     * TRUE.  If the latest state does not match the actual state of the partition, the method should
     * return FALSE.  If any bad occurs during verification, the method may throw an exception.
     * @throws Exception Could be caused by bad configuration, an nested exception propagated during
     * the verification check, etc.
     */
    boolean verify() throws Exception;

    /**
     * Add a listener to the Partition
     * @param listener
     */
    void addListener(Listener listener);

    /**
     * Remove a listener from the Partition
     * @param listener
     */
    void removeListener(Listener listener);

    /**
     * Listener for life cycle events on the partition.
     */
    public interface Listener {

        /**
         * Called when the partition changes state.
         * @param thisPartition The partition changing (usually this).
         * @param oldState The state the partition is moving away from.
         * @param newState The state the partition is moving to.
         * @param reasonForChange The reason why the partition has changed state.
         */
        void onPartitionStateChange(
                Partition thisPartition,
                PartitionStates oldState,
                PartitionStates newState,
                String reasonForChange);
    }
}
