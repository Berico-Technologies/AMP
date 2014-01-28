package amp.topology.global;

import amp.topology.global.exceptions.PartitionAlreadyExistsException;
import amp.topology.global.exceptions.PartitionNotExistException;
import amp.topology.global.filtering.RouteRequirements;
import amp.topology.global.lifecycle.LifeCycleObservationManager;
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
 * Handles some of the boilerplate in implementing a TopologyGroup.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public abstract class TopologyGroup<PARTITION extends Partition> {

    protected final TypeToken<PARTITION> PARTITION_TYPE = new TypeToken<PARTITION>(){};

    /**
     * Provide a default, unique identifier for the Group that can
     * be overridden by inheriting implementations.
     */
    private String id = UUID.randomUUID().toString();

    private String description = "";

    private ConcurrentMap<String, PARTITION> partitions = Maps.newConcurrentMap();

    /**
     * Set the id of the Topology Group.
     * @param id Id of the Group.
     */
    protected void setId(String id){

        this.id = id;
    }

    /**
     * Get the id of the Topology Group.
     * @return Id of the Group.
     */
    public String getId() {

        return this.id;
    }

    /**
     * Set the description of the Group.
     * @param description Friendly description.
     */
    public void setDescription(String description) {

        this.description = description;
    }

    /**
     * Get a description of this Group.
     * @return
     */
    public String getDescription() {

        return this.description;
    }

    /**
     * Get applicable partitions based on a Request.  It is up to the Group to decide whether
     * a client should receive partitions, and which partitions those should be.
     *
     * @param requirements Client Requirements
     * @return
     */
    public abstract Collection<PARTITION> filter(RouteRequirements requirements);

    /**
     * Called when the Group is instantiated.  This is an opportunity for the Group to do whatever it needs to do
     * (like create an initial partition) in order to become ready.  This method should be Idempotent.  If called
     * more than once, it should not setup up double the infrastructure, nor should it throw an exception if
     * that infrastructure/configuration is already intact and valid.
     * @throws Exception An error encountered during the setup process.
     */
    public abstract void setup() throws Exception;

    /**
     * Called when the Group is being removed.  The Group is given a chance to shutdown and clean up partitions
     * it may have provisioned during the course of its life.
     * @throws Exception An error encountered during the cleanup process.
     */
    public abstract void cleanup() throws Exception;

    /**
     * Get a Partition by Id.
     * @param id Id of the partition to retrieve.
     * @return
     * @throws PartitionNotExistException
     */
    @ExceptionMetered(cause = PartitionNotExistException.class)
    public PARTITION getPartition(String id) throws PartitionNotExistException {

        PARTITION part = partitions.get(id);

        if (part == null) throw new PartitionNotExistException(this.getId(), id);

        return part;
    }

    /**
     * Get the Partitions managed by this group.
     * @return Unmodifiable collection of Partitions maintained by the Group.
     */
    @Metered
    public Collection<PARTITION> getPartitions() {

        return partitions.values();
    }

    /**
     * Add a partition to the set of Partitions.  This method will call "setup" on
     * the Partition for you, as well as, fire all of the listeners.
     * @param partition Partition to Add
     * @throws Exception Does not handle the exception that may arise on "setup".
     */
    public void addPartition(PARTITION partition) throws Exception {

        PARTITION oldPartition = partitions.putIfAbsent(partition.getId(), partition);

        if (oldPartition == null){

            partition.setup();

            LifeCycleObservationManager.fireOnAdded(partition);

        } else {

            throw new PartitionAlreadyExistsException(this.getId(), partition.getId());
        }
    }

    /**
     * Remove a Partition with the supplied ID.  The Partition's "cleanup" method
     * will be called by this method, and all listeners will be notified.
     * @param partitionId ID of the Partition to cleanup.
     * @throws Exception Error encountered during cleanup, or a PartitionNotExistException
     */
    protected void removePartition(String partitionId) throws Exception {

        removePartition(getPartition(partitionId));

        PARTITION partition = partitions.remove(partitionId);

        if (partition == null) throw new PartitionNotExistException(this.getId(), partition.getId());

        partition.cleanup();

        LifeCycleObservationManager.fireOnRemoved(partition);
    }

    /**
     * Remove a Partition.  The Partition's "cleanup" method
     * will be called by this method, and all listeners will be notified.
     * @param partition The Partition to cleanup.
     * @throws Exception Error encountered during cleanup, or a PartitionNotExistException
     */
    protected void removePartition(PARTITION partition) throws Exception {

        removePartition(partition.getId());
    }

    /**
     * Mandatory state properties for a Group.
     */
    public static class HydratedState extends TopologyState {

        private String groupId;

        private boolean isConsumerGroup = false;

        private Set<String> partitionIds = Sets.newHashSet();

        public HydratedState(
                Class<? extends TopologyGroup> groupType,
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
