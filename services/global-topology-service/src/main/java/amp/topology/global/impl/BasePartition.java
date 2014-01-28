package amp.topology.global.impl;

import amp.topology.global.Partition;
import amp.topology.global.lifecycle.LifeCycleObserver;
import amp.topology.global.persistence.PersistenceManager;
import com.yammer.metrics.annotation.Timed;

import java.util.UUID;

/**
 * Handles some of the Boiler Plate of implementing a BasePartition.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public abstract class BasePartition extends BaseTopologyItem<BasePartition.DehydratedState> implements Partition {

    /**
     * Provide a default, unique identifier for the BasePartition that can
     * be overridden by inheriting implementations.
     */
    private String partitionId = UUID.randomUUID().toString();

    private String groupId;

    private PartitionStates partitionState = PartitionStates.NONEXISTENT;

    /**
     * Set the ID of the BasePartition.
     * @param id ID of the partition.
     */
    @Override
    public void setPartitionId(String id){

        this.partitionId = id;
    }

    /**
     * Get the ID of the BasePartition.
     * @return
     */
    @Override
    public String getPartitionId() {

        return this.partitionId;
    }

    @Override
    public void setGroupId(String groupId) {

        this.groupId = groupId;
    }

    @Override
    public String getGroupId() {

        return groupId;
    }

    public void save(){

        DehydratedState hydratedState =
                new DehydratedState(getClass(), getTopicId(), getDescription(), getGroupId(), getPartitionId());

        hydratedState.getExtensionProperties().putAll(getExtensionProperties());

        PersistenceManager.partitions().save(hydratedState);
    }

    @Override
    public void restore(DehydratedState state) {

        setPartitionId(state.getPartitionId());
        setGroupId(state.getGroupId());

        super.restore(state);
    }


    /**
     * Get the latest state of the partition.
     * @return Current partition state.
     */
    @Override
    public PartitionStates getState() {

        return this.partitionState;
    }

    /**
     * Set the state of the BasePartition.  This is how derived classes set the BasePartition state
     * and notifies listeners.
     * @param newState New BasePartition State
     * @param reasonForChange A reason for the change.
     */
    @Timed
    protected void setState(PartitionStates newState, String reasonForChange){

        PartitionStates oldState = this.partitionState;

        this.partitionState = newState;

        LifeCycleObserver.fireOnStateChanged(this, oldState, newState, reasonForChange);
    }

    /**
     * Mandatory state properties for a BasePartition.
     */
    public static class DehydratedState extends TopologyState {

        private String groupId;

        private String partitionId;

        public DehydratedState(
                Class<? extends BasePartition> partitionType,
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
