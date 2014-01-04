package amp.topology.global.impl;

import amp.topology.global.Partition;
import com.google.common.collect.Sets;
import com.yammer.metrics.annotation.Timed;

import java.util.Set;
import java.util.UUID;

/**
 * Handles some of the Boiler Plate of implementing a Partition.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public abstract class BasePartition implements Partition {

    /**
     * Provide a default, unique identifier for the Partition that can
     * be overridden by inheriting implementations.
     */
    private String id = UUID.randomUUID().toString();

    private PartitionStates partitionState = PartitionStates.NONEXISTENT;

    private Set<Listener> listeners = Sets.newCopyOnWriteArraySet();

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
    @Override
    public String getId() {

        return this.id;
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
     * Set the state of the Partition.  This is how derived classes set the Partition state
     * and notifies listeners.
     * @param newState New Partition State
     * @param reasonForChange A reason for the change.
     */
    @Timed
    protected void setState(PartitionStates newState, String reasonForChange){

        PartitionStates oldState = this.partitionState;

        this.partitionState = newState;

        for (Listener listener : listeners)
            if (listener != null) listener.onPartitionStateChange(this, oldState, newState, reasonForChange);
    }

    /**
     * Add a listener to the partition. Nulls and existing entries will be ignored.
     * @param listener Listener to add
     */
    @Override
    public void addListener(Listener listener) {

        if (listener != null && !listeners.contains(listener)) listeners.add(listener);
    }

    /**
     * Remove a listener from the partition. Nulls and entries not currently registered will be ignored.
     * @param listener
     */
    @Override
    public void removeListener(Listener listener) {

        if (listener != null && listeners.contains(listener)) listeners.remove(listener);
    }
}
