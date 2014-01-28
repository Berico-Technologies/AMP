package amp.topology.global;

import amp.topology.global.Partition;
import amp.topology.global.lifecycle.LifeCycleObservationManager;
import amp.topology.global.lifecycle.PersistenceManager;
import com.google.common.collect.Sets;
import com.yammer.metrics.annotation.Timed;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Handles some of the Boiler Plate of implementing a Partition.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public abstract class Partition {

    /**
     * Provide a default, unique identifier for the Partition that can
     * be overridden by inheriting implementations.
     */
    private String id = UUID.randomUUID().toString();

    private String topicId;

    private String groupId;

    private PartitionStates partitionState = PartitionStates.NONEXISTENT;

    /**
     * Set the ID of the Partition.
     * @param id ID of the partition.
     */
    protected void setId(String id){

        this.id = id;
    }

    /**
     * Get the ID of the Partition.
     * @return
     */
    public String getId() {

        return this.id;
    }

    public String getTopicId() {
        return topicId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void save(){

        HydratedState hydratedState =
                new HydratedState(getClass(), getTopicId(), getDescription(), getGroupId(), getId());

        PersistenceManager.partitions().save(hydratedState);
    }

    public void restore(HydratedState state){

        this.topicId = state.getTopicId();
        this.groupId = state.getGroupId();
        this.id = state.getPartitionId();

        restoreFromProperties(state.getExtensionProperties());
    }

    public abstract void restoreFromProperties(Map<String, String> properties);

    /**
     * Some friendly info about this Partition.  Since it is not created by a user, this may be
     * something similar to a "toString" method.
     * @return  Friendly description.
     */
    public abstract String getDescription();

    /**
     * Attempt to activate the partition.
     * @throws Exception could represent an error in attempting to achieve the active state,
     * like a misconfiguration or inability to provision resources.
     */
    public abstract void activate() throws Exception;

    /**
     * Deactive the partition.
     * @throws Exception could represent an error in attempting to achieve the inactive state,
     * like having active clients and not being able to cleanup them.
     */
    public abstract void deactive() throws Exception;

    /**
     * Called when the Partition is instantiated.  This is an opportunity for the Partition to provision any resources
     * it may need to function before becoming active.
     * @throws Exception An error encountered during the setup process.
     */
    public abstract void setup() throws Exception;

    /**
     * Called when the Partition is being removed.  The Partition is given a chance to cleanup any configuration
     * it has left on the system.
     * @throws Exception An error encountered during the cleanup process.
     */
    public abstract void cleanup() throws Exception;

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
     * Verify the state of the partition.  If the state of the partition is invalid, throw an exception.
     *
     * It's expected that the state of the partition be updated during the verification.
     *
     * @throws Exception Could be caused by bad configuration, an nested exception propagated during
     * the verification check, etc.
     */
    public abstract void verify() throws Exception;


    /**
     * Get the latest state of the partition.
     * @return Current partition state.
     */
    public PartitionStates getState() {

        return this.partitionState;
    }

    /**
     * Set the state of the Partition.  This is how derived classes set the Partition state
     * and notifies listeners.
     * @param newState New Partition State
     * @param reasonForChange A reason for the change.
     */
    @Timed
    protected void setState(PartitionStates newState, String reasonForChange){

        PartitionStates oldState = this.partitionState;

        this.partitionState = newState;

        LifeCycleObservationManager.fireOnStateChanged(this, oldState, newState, reasonForChange);
    }

    /**
     * Mandatory state properties for a Partition.
     */
    public static class HydratedState extends TopologyState {

        private String groupId;

        private String partitionId;

        public HydratedState(
                Class<? extends Partition> partitionType,
                String topicId,
                String description,
                String groupId,
                String partitionId) {

            super(partitionType, topicId, description);
            this.groupId = groupId;
            this.partitionId = partitionId;
        }

        public String getGroupId() {
            return groupId;
        }

        public String getPartitionId() {
            return partitionId;
        }
    }
}
