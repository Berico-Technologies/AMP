package amp.topology.global;

import amp.topology.anubis.AccessControlList;
import amp.topology.global.exceptions.PartitionNotExistException;
import amp.topology.global.filtering.RouteRequirements;
import com.google.common.reflect.TypeToken;

import java.util.Collection;

/**
 * Manages a homogeneous group of Partitions.
 *
 * At the very least, this 'homogeneous' means they partitions have
 * the same AccessControls.  More than likely, this will also mean they
 * use the same protocol and are intended to connect to a preordained set
 * of groups.
 *
 * The TopologyGroup is responsible for managing the life cycle of it's
 * partitions, which includes calling life cycle methods like setup, cleanup,
 * active, and deactive.
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
     * Get applicable partitions based on a Request.  It is up to the Group to decide whether
     * a client should receive partitions, and which partitions those should be.
     *
     * @param requirements Client Requirements
     * @return
     */
    Collection<PARTITION> filter(RouteRequirements requirements);


    /**
     * Called when the Group is instantiated.  This is an opportunity for the Group to do whatever it needs to do
     * (like create an initial partition) in order to become ready.
     * @throws Exception An error encountered during the setup process.
     */
    void setup() throws Exception;

    /**
     * Called when the Group is being removed.  The Group is given a chance to shutdown and clean up partitions
     * it may have provisioned during the course of its life.
     * @throws Exception An error encountered during the cleanup process.
     */
    void cleanup() throws Exception;


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
     * @param listener Listener to cleanup.
     */
    void removeListener(Listener listener);

    /**
     * Called when partitions are added or removed from the group.
     *
     * Because of funkiness with Generics, you will need to cast the Partition to the
     * correct type if you want to access implementation specific behavior.
     */
    public interface Listener {
        /**
         * Fired when a partition is added.
         * @param partitionToAdd Partition to add.
         */
        void onPartitionAdded(Partition partitionToAdd);

        /**
         * Fired when a partition is removed.
         * @param partitionToRemove Partition to cleanup.
         */
        void onPartitionRemoved(Partition partitionToRemove);
    }
}
