package amp.topology.global;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public interface Partition extends TopologyItem {

    void setPartitionId(String id);

    String getPartitionId();

    void setGroupId(String groupId);

    String getGroupId();

    /**
     * Verify the state of the partition.  If the state of the partition is invalid, throw an exception.
     *
     * It's expected that the state of the partition be updated during the verification.
     *
     * @throws Exception Could be caused by bad configuration, an nested exception propagated during
     * the verification check, etc.
     */
    void verify() throws Exception;


    PartitionStates getState();

    /**
     * The states a BasePartition can exist in.
     */
    public enum PartitionStates {
        /**
         * The BasePartition has never been setup or activated.
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
}
