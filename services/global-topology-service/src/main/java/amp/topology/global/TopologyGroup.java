package amp.topology.global;

import amp.topology.global.anubis.AccessControlList;
import amp.topology.global.exceptions.PartitionNotExistException;

import java.util.Collection;

/**
 * Manages a homogeneous group of Partitions.
 *
 * At the very least, this 'homogeneous' means they partitions have
 * the same AccessControls.  More than likely, this will also mean they
 * use the same protocol and are intended to connect to a preordained set
 * of groups.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public interface TopologyGroup<PARTITION extends Partition> {

    /**
     * The id of this group (must at least be unique to the TopicSpace),
     * but could be globally unique depending on data store implementation.
     * @return id
     */
    String getId();

    /**
     * Something descriptive about this group.
     * @return Description
     */
    String getDescription();

    /**
     * Permissions related to this Group.
     * @return Access Control List.
     */
    AccessControlList getACL();

    /**
     * Retrieve a partition by id.
     * @param id Id of the partition to retrieve.
     * @return Partition
     * @throws PartitionNotExistException if the id is to a partition that does not exist within this group.
     */
    PARTITION getPartition(String id) throws PartitionNotExistException;

    /**
     * Retrieve all the partitions managed by this group.
     * @return Collection of partitions
     */
    Collection<PARTITION> getPartitions();


    /**
     * Get applicable partitions based on a Request.
     * @return
     */
    Collection<PARTITION> filter();

    // TODO: Implement metrics based expansion/contraction
    // Expansion and contraction will be implementation specific in the meantime.
    //void observe(PartitionMetrics metrics);


    /**
     * Add a listener to this group.
     * @param listener Listener to add.
     */
    void addListener(Listener listener);

    /**
     * Remove a Listener
     * @param listener Listener to remove.
     */
    void removeListener(Listener listener);

    /**
     * Called when partitions are added or removed from the group.
     */
    public interface Listener {

        /**
         * Fired when a partition is added.
         * @param partitionToAdd Partition to add.
         */
        void onPartitionAdded(PARTITION partitionToAdd);

        /**
         * Fired when a partition is removed.
         * @param partitionToRemove Partition to remove.
         */
        void onPartitionRemoved(PARTITION partitionToRemove);
    }
}
