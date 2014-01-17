package amp.topology.global.impl;

import amp.topology.anubis.AccessControl;
import amp.topology.anubis.AccessControlList;
import amp.topology.global.Partition;
import amp.topology.global.TopologyGroup;
import amp.topology.global.exceptions.PartitionAlreadyExistsException;
import amp.topology.global.exceptions.PartitionNotExistException;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.yammer.metrics.annotation.ExceptionMetered;
import com.yammer.metrics.annotation.Metered;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

/**
 * Handles some of the boilerplate in implementing a TopologyGroup.
 *
 * Synchronization Policy:  We are assuming that the number of mutations
 * committed against Listeners  will be relatively low, so we
 * are preferring a CopyOnWriteArraySet over a Concurrent collection.
 *
 * As for the partitions collection, we are using a private re-entrant lock
 * that can only be held by the Base Class to encapsulate CRUD against
 * the Partitions collection.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public abstract class BaseTopologyGroup<PARTITION extends Partition> implements TopologyGroup<PARTITION> {

    protected final TypeToken<PARTITION> PARTITION_TYPE = new TypeToken<PARTITION>(){};

    /**
     * Provide a default, unique identifier for the Group that can
     * be overridden by inheriting implementations.
     */
    private String id = UUID.randomUUID().toString();

    private String description = "";

    private Set<Listener> listeners = Sets.newCopyOnWriteArraySet();

    private Object partitionsLock = new Object();

    private Set<PARTITION> partitions = Sets.newCopyOnWriteArraySet();

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
    @Override
    public String getId() {

        return this.id;
    }

    /**
     * Set the description of the Group.
     * @param description Friendly description.
     */
    @Override
    public void setDescription(String description) {

        this.description = description;
    }

    /**
     * Get a description of this Group.
     * @return
     */
    @Override
    public String getDescription() {

        return this.description;
    }

    /**
     * Get a Partition by Id.
     * @param id Id of the partition to retrieve.
     * @return
     * @throws PartitionNotExistException
     */
    @Override
    @ExceptionMetered(cause = PartitionNotExistException.class)
    public PARTITION getPartition(String id) throws PartitionNotExistException {

        for (PARTITION partition : partitions){

            if (partition.getId().equals(id)) return partition;
        }

        throw new PartitionNotExistException(this.getId(), id);
    }

    /**
     * Get the Partitions managed by this group.
     * @return Unmodifiable collection of Partitions maintained by the Group.
     */
    @Override
    @Metered
    public Collection<PARTITION> getPartitions() {

        return Collections.unmodifiableCollection(partitions);
    }

    /**
     * Add a partition to the set of Partitions.  This method will call "setup" on
     * the Partition for you, as well as, fire all of the listeners.
     * @param partition Partition to Add
     * @throws Exception Does not handle the exception that may arise on "setup".
     */
    protected void addPartition(PARTITION partition) throws Exception {

        synchronized (partitionsLock) {

            if (!containsPartition(partition)){

                partition.setup();

                partitions.add(partition);

                for (Listener listener : listeners)
                    if (listener != null) listener.onPartitionAdded(partition);
            }
            else {

                throw new PartitionAlreadyExistsException(this.getId(), partition.getId());
            }
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
    }

    /**
     * Remove a Partition.  The Partition's "cleanup" method
     * will be called by this method, and all listeners will be notified.
     * @param partition The Partition to cleanup.
     * @throws Exception Error encountered during cleanup, or a PartitionNotExistException
     */
    protected void removePartition(PARTITION partition) throws Exception {

        synchronized (partitionsLock) {

            // We want to make sure we have the real object.
            if (partitions.contains(partition)){

                partition.cleanup();

                partitions.remove(partition);

                for (Listener listener : listeners)
                    if (listener != null) listener.onPartitionRemoved(partition);
            }
            else {

                throw new PartitionNotExistException(this.getId(), partition.getId());
            }
        }
    }

    /**
     * Determine if the Partitions set contains a Partition by either identity or
     * by ID.
     * @param target Partition to query for.
     * @return TRUE if the partition is contained in the internal Partitions set.
     */
    protected boolean containsPartition(PARTITION target){

        synchronized (partitionsLock) {

            if (partitions.contains(target)) return true;

            return containsPartition(target.getId());
        }
    }

    /**
     * Determine if the Partitions set has a Partition with the supplied ID.
     * @param id ID of the Partition to Query for.
     * @return TRUE if the Partitions set does contain the Partition.
     */
    public boolean containsPartition(String id){

        synchronized (partitionsLock) {

            for (PARTITION partition : partitions)
                if (partition.getId().equals(id)) return true;
        }

        return false;
    }

    /**
     * Add a Listener to the Group.
     * @param listener Listener to add.
     */
    @Override
    public void addListener(Listener listener) {

        if (listener != null && !listeners.contains(listener)) listeners.add(listener);
    }

    /**
     * Remove Listener from the Group.
     * @param listener Listener to cleanup.
     */
    @Override
    public void removeListener(Listener listener) {

        if (listener != null && listeners.contains(listener)) listeners.remove(listener);
    }
}
