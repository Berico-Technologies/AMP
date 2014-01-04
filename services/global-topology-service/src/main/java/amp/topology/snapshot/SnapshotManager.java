package amp.topology.snapshot;

import amp.topology.snapshot.exceptions.TopicConfigurationChangeExceptionRollup;
import amp.topology.snapshot.exceptions.SnapshotDoesNotExistException;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * The entity responsible for managing topology snapshots.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public interface SnapshotManager {

    /**
     * Take a snapshot of the latest state of Topology and return it
     * to the caller.
     * @param description An optional description of why the snapshot was made.  If you
     *                    do not want to add a description, the parameter may be null.
     * @return Current state of topology.
     * @throws Exception an error encountered during the persistence of the snapshot.
     */
    @PreAuthorize("hasRole('gts-snapshot-export')")
    Snapshot export(@Nullable String description) throws Exception;

    /**
     * Get the latest snapshot of the topology.  This isn't the latest topology, but
     * rather the topology as it existed during the last export.
     *
     * The return value can be null!  Particularly if a snapshot was never taken, or if
     * the snapshots are purged from whatever underlying storage technology used to
     * implement the SnapshotManager.
     *
     * @return Last exported state of the topology.
     */
    @PreAuthorize("hasRole('gts-snapshot-describe')")
    @Nullable Snapshot latest();

    /**
     * Get the last time the topology was persisted.
     * @return last time the topology was persisted
     */
    @PreAuthorize("hasRole('gts-snapshot-info')")
    long lastPersisted();

    /**
     * Get a snapshot by id.
     * @param snapshotId Id of the snapshot to retrieve.
     * @return Snapshot
     * @throws SnapshotDoesNotExistException encountered if the supplied ID does not match
     * an existing snapshot.
     */
    @PreAuthorize("hasRole('gts-snapshot-describe')")
    Snapshot get(String snapshotId) throws Exception;

    /**
     * Return a list of descriptors for all available snapshots.
     * @return All available snapshots.
     */
    @PreAuthorize("hasRole('gts-snapshot-list')")
    Collection<SnapshotDescriptor> list();

    /**
     * Overwrite the existing topology with the provided Snapshot.
     *
     * Notes:  If a TopicConfiguration in the Snapshot is considered equal to the
     * TopicConfiguration in the TopicRegistry, no action will occur.
     *
     * At the end of the operation, any TopicConfigurations in the TopologyRegistry that do not have
     * a corresponding entry in the Snapshot will be removed!!!!  If you do not want this behavior,
     * use the merge() method.
     *
     * There is no transactional guarantee for this operation.  Simply put, this is not an all or nothing
     * operation.  Also, this method will continue to attempt to modify the topology even if encounters
     * an error modifying a leaf in the topic tree.
     *
     *
     * @param snapshot Snapshot to use as configuration.
     * @throws TopicConfigurationChangeExceptionRollup a composite of errors that occurred during the operation.
     */
    @PreAuthorize("hasRole('gts-snapshot-overwrite')")
    void overwrite(Snapshot snapshot) throws TopicConfigurationChangeExceptionRollup;

    /**
     * Overwrite the existing topology with the provided Snapshot.
     *
     * Notes:  If a TopicConfiguration in the Snapshot is considered equal to the
     * TopicConfiguration in the TopicRegistry, no action will occur.
     *
     * There is no transactional guarantee for this operation.  Simply put, this is not an all or nothing
     * operation.  Also, this method will continue to attempt to modify the topology even if encounters
     * an error modifying a leaf in the topic tree.
     *
     * @param snapshot Snapshot to merge.
     * @throws Exception a composite of errors that occurred during the operation..
     */
    @PreAuthorize("hasRole('gts-snapshot-merge')")
    void merge(Snapshot snapshot) throws TopicConfigurationChangeExceptionRollup;
}
