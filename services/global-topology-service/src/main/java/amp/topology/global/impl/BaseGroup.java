package amp.topology.global.impl;

import amp.topology.global.Group;
import amp.topology.global.Partition;
import amp.topology.global.exceptions.PartitionAlreadyExistsException;
import amp.topology.global.exceptions.PartitionNotExistException;
import amp.topology.global.lifecycle.LifeCycleObserver;
import amp.topology.global.persistence.PersistenceManager;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.yammer.metrics.annotation.ExceptionMetered;
import com.yammer.metrics.annotation.Metered;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

/**
 * Handles some of the boilerplate in implementing a BaseGroup.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public abstract class BaseGroup<PARTITION extends BasePartition> extends BaseTopologyItem<BaseGroup.DehydratedState> implements Group<PARTITION> {

    protected final TypeToken<PARTITION> PARTITION_TYPE = new TypeToken<PARTITION>(getClass()){};

    /**
     * Provide a default, unique identifier for the BaseGroup that can
     * be overridden by inheriting implementations.
     */
    private String groupId = UUID.randomUUID().toString();

    private ConcurrentMap<String, PARTITION> partitions = Maps.newConcurrentMap();

    /**
     * Set the groupId of the Topology BaseGroup.
     * @param id Id of the BaseGroup.
     */
    @Override
    public void setGroupId(String id){

        this.groupId = id;
    }

    /**
     * Get the groupId of the Topology BaseGroup.
     * @return Id of the BaseGroup.
     */
    @Override
    public String getGroupId() {

        return this.groupId;
    }


    /**
     * Persist the Topology BaseGroup state.
     */
    @Override
    public void save(){

        save(true);
    }

    @Override
    public void save(boolean saveAggregates) {

        if (saveAggregates)
            for (Partition partition : partitions.values()) partition.save();

        LifeCycleObserver.fireOnSaved(this);
    }

    @Override
    public DehydratedState dehydrate() {

        DehydratedState state =
                new DehydratedState(
                        getClass(),
                        getTopicId(),
                        getGroupId(),
                        getDescription(),
                        BaseConsumerGroup.class.isAssignableFrom(getClass()),
                        partitions.keySet());

        state.getExtensionProperties().putAll(getExtensionProperties());

        return state;
    }

    /**
     * Restore the BaseGroup from the Dehydrated State
     * @param state
     */
    @Override
    public void restore(DehydratedState state) {

        setGroupId(state.getGroupId());

        // Partitions will be hydrated by an external entity.

        super.restore(state);
    }

    /**
     * Get a BasePartition by Id.
     * @param id Id of the partition to retrieve.
     * @return
     * @throws PartitionNotExistException
     */
    @Override
    @ExceptionMetered(cause = PartitionNotExistException.class)
    public PARTITION getPartition(String id) throws PartitionNotExistException {

        PARTITION part = partitions.get(id);

        if (part == null) throw new PartitionNotExistException(this.getGroupId(), id);

        return part;
    }

    /**
     * Get the Partitions managed by this group.
     * @return Unmodifiable collection of Partitions maintained by the BaseGroup.
     */
    @Override
    @Metered
    public Collection<PARTITION> getPartitions() {

        return partitions.values();
    }

    /**
     * Add a partition to the set of Partitions.  This method will call "setup" on
     * the BasePartition for you, as well as, fire all of the listeners.
     * @param partition BasePartition to Add
     * @throws Exception Does not handle the exception that may arise on "setup".
     */
    @Override
    public void addPartition(PARTITION partition) throws Exception {

        PARTITION oldPartition = partitions.putIfAbsent(partition.getPartitionId(), partition);

        if (oldPartition == null){

            partition.setup();

            LifeCycleObserver.fireOnAdded(partition);

        } else {

            throw new PartitionAlreadyExistsException(this.getGroupId(), partition.getPartitionId());
        }
    }

    /**
     * Remove a BasePartition with the supplied ID.  The BasePartition's "cleanup" method
     * will be called by this method, and all listeners will be notified.
     * @param partitionId ID of the BasePartition to cleanup.
     * @throws Exception Error encountered during cleanup, or a PartitionNotExistException
     */
    @Override
    public void removePartition(String partitionId) throws Exception {

        PARTITION partition = partitions.remove(partitionId);

        if (partition == null) throw new PartitionNotExistException(this.getGroupId(), partitionId);

        partition.cleanup();

        LifeCycleObserver.fireOnRemoved(partition);
    }

    /**
     * Remove a BasePartition.  The BasePartition's "cleanup" method
     * will be called by this method, and all listeners will be notified.
     * @param partition The BasePartition to cleanup.
     * @throws Exception Error encountered during cleanup, or a PartitionNotExistException
     */
    void removePartition(PARTITION partition) throws Exception {

        removePartition(partition.getPartitionId());
    }

    /**
     * For hydration purposes, set partitions without running "setup()".
     * @param partitions Existing partitions recreated after storage.
     */
    public void setPartitions(Collection<? extends BasePartition> partitions){

        for (BasePartition partition : partitions)
            this.partitions.put(partition.getPartitionId(), (PARTITION) partition);
    }

    /**
     * Mandatory state properties for a BaseGroup.
     */
    public static class DehydratedState extends TopologyState {

        private String groupId;

        private boolean isConsumerGroup = false;

        private Set<String> partitionIds = Sets.newHashSet();

        public DehydratedState(
                Class<? extends Group> groupType,
                String topicId,
                String groupId,
                String description,
                boolean consumerGroup,
                Collection<String> partitionIds) {

            super(groupType, topicId, description);
            this.groupId = groupId;
            this.isConsumerGroup = consumerGroup;
            this.partitionIds.addAll(partitionIds);
        }

        public String getGroupId() {
            return groupId;
        }

        public boolean isConsumerGroup() {
            return isConsumerGroup;
        }

        public Set<String> getPartitionIds() {
            return partitionIds;
        }
    }
}
