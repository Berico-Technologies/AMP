package amp.topology.snapshot.impl;

import amp.topology.global.TopicConfiguration;
import amp.topology.global.TopicRegistry;
import amp.topology.snapshot.Snapshot;
import amp.topology.snapshot.SnapshotDescriptor;
import amp.topology.snapshot.SnapshotManager;
import amp.topology.snapshot.exceptions.SnapshotDoesNotExistException;
import amp.topology.snapshot.exceptions.TopicConfigurationChangeExceptionRollup;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

/**
 * Persists snapshots to the file system.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class FileSystemSnapshotManager implements SnapshotManager {

    private static final Logger logger = LoggerFactory.getLogger(FileSystemSnapshotManager.class);

    TopicRegistry topicRegistry;

    SnapshotSerializer serializer;

    File snapshotDirectory;

    Snapshot currentSnapshot;

    /**
     * Instantiate the FileSystemSnapshotManager with the TopicRegistry, desired serializer, and
     * location of the snapshot directory.
     * @param snapshotDirectory Directory to store the snapshots.
     * @param topicRegistry The topic registry.
     * @param serializer The serializer to use for the snapshot objects.
     */
    public FileSystemSnapshotManager(TopicRegistry topicRegistry, SnapshotSerializer serializer, File snapshotDirectory) {
        this.topicRegistry = topicRegistry;
        this.serializer = serializer;
        this.snapshotDirectory = snapshotDirectory;

        validate();
    }

    /**
     * Validate the state of the SnapshotManager.
     */
    private void validate() {

        Preconditions.checkNotNull(this.topicRegistry);
        Preconditions.checkNotNull(this.serializer);
        Preconditions.checkNotNull(this.snapshotDirectory);

        if (!this.snapshotDirectory.exists()){

            this.snapshotDirectory.mkdirs();
        }
        else {

            Preconditions.checkState(this.snapshotDirectory.isDirectory());

        }

        Preconditions.checkState(this.snapshotDirectory.canRead());
        Preconditions.checkState(this.snapshotDirectory.canWrite());
    }

    /**
     * Export the existing state of the Topology to the disk, returning a copy to the
     * requester.
     * @param description An optional description of why the snapshot was made.  If you
     *                    do not want to add a description, the parameter may be null.
     * @return
     * @throws Exception
     */
    @Override
    public Snapshot export(@Nullable String description) throws Exception {

        ArrayList<TopicConfiguration> topics = Lists.newArrayList();

        for(TopicConfiguration topic : topicRegistry.entries()){

            topics.add(topic);
        }

        Snapshot snapshot = new Snapshot(UUID.randomUUID().toString(), description, System.currentTimeMillis(), topics);

        String serializedSnapshot = serializer.serialize(snapshot);

        writeToFileSystem(serializedSnapshot, getInstanceFile(snapshot));

        writeToFileSystem(serializedSnapshot, getLatestFile());

        this.currentSnapshot = snapshot;

        return snapshot;
    }


    /**
     * Get the latest snapshot.  This method is lazy and will attempt to dehydrate the snapshot from the file system
     * if the value of the currentSnapshot is null.  If the latest snapshot file is not found, this method will return
     * Null.
     * @return Latest snapshot file or null.
     */
    @Override
    public @Nullable Snapshot latest() {

        if (this.currentSnapshot == null){

            try {

                this.currentSnapshot = dehydrateLatestSnapshot();

            } catch (Exception e){

                //logger.error("A problem was encountered attempting to retrieve the latest snapshot.", e);
            }
        }

        return this.currentSnapshot;
    }

    /**
     * Get the last time a snapshot was taken.
     * @return last time a snapshot was taken, or -1 if there is no latest snapshots.
     */
    @Override
    public long lastPersisted() {

        if (this.latest() == null)
            return -1;

        return  this.latest().getTimestamp();
    }

    /**
     * Get the snapshot with the specified id.
     * @param snapshotId Id of the snapshot to retrieve.
     * @return Snapshot
     * @throws Exception An exception if the snapshot does not exist, or an exception if there is a problem
     * encountered attempting to dehydrate the snapshot from the file system.
     */
    @Override
    public Snapshot get(String snapshotId) throws Exception {

        File snapshotFile = locateInstanceFile(snapshotId);

        if (snapshotFile == null)
            throw new SnapshotDoesNotExistException(snapshotId);

        Snapshot snapshot = dehydrateSnapshot(snapshotFile);

        if (snapshot == null)
            throw new SnapshotDoesNotExistException(snapshotId);

        return snapshot;
    }

    /**
     * Iterate through the snapshot directory, locating the file with the
     * supplied snapshot id.
     * @param snapshotId Id of the snapshot to locate.
     * @return File (if found) or null.
     */
    @Nullable File locateInstanceFile(String snapshotId) {

        Iterator<File> fileIterator =
                FileUtils.iterateFiles(
                        this.snapshotDirectory,
                        new String[]{serializer.serializedFileExtension()},
                        false);

        while (fileIterator.hasNext()) {

            File file = fileIterator.next();

            FilenameSnapshotDescriptor snapshotDescriptor = null;

            try {

                snapshotDescriptor = FilenameSnapshotDescriptor.fromFile(file);
            }
            catch (Exception e){
                // Probably not a snapshot file.
                logger.trace(
                        "Skipping file '{}' because it doesn't conform to snapshot 'instance' file conventions.",
                        file.getName());
            }

            if (snapshotDescriptor != null && snapshotDescriptor.getId().equals(snapshotId))
                return file;
        }

        return null;
    }

    /**
     * Iterates through the snapshot directory finding valid snapshots.
     * @return Collection of descriptors that describe the available snapshots.
     */
    @Override
    public Collection<SnapshotDescriptor> list() {

        ArrayList<SnapshotDescriptor> snapshotDescriptors = Lists.newArrayList();

        Iterator<File> fileIterator =
                FileUtils.iterateFiles(
                        this.snapshotDirectory,
                        new String[]{ serializer.serializedFileExtension() },
                        false);

        while (fileIterator.hasNext()) {

            File file = fileIterator.next();

            try {

                FilenameSnapshotDescriptor snapshotDescriptor = FilenameSnapshotDescriptor.fromFile(file);

                snapshotDescriptors.add(snapshotDescriptor);
            }
            catch (Exception e){
                // Probably not a snapshot file.
                logger.trace(
                        "Skipping file '{}' because it doesn't conform to snapshot 'instance' file conventions.",
                        file.getName());
            }
        }

        return snapshotDescriptors;
    }

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
     * @param snapshot Snapshot to use as configuration.
     * @throws amp.topology.snapshot.exceptions.TopicConfigurationChangeExceptionRollup a composite of errors that occurred during the operation.
     */
    @Override
    public void overwrite(Snapshot snapshot) throws TopicConfigurationChangeExceptionRollup {

        synchronizeTopology(snapshot, true);
    }

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
    @Override
    public void merge(Snapshot snapshot) throws TopicConfigurationChangeExceptionRollup {

        synchronizeTopology(snapshot, false);
    }

    /**
     * Performs the synchronization of the topology tree.
     *
     * Synchronization policy:  the snapshotter will attempt to lock the TopicRegistry to prevent outside modification
     * of the Topology while the synchronization is in progress.
     *
     * @param snapshot Snapshot to synchronize
     * @param removeUnspecifiedEntries If TRUE, entries not in the Snapshot will be removed from the TopicRegistry.
     * @throws amp.topology.snapshot.exceptions.TopicConfigurationChangeExceptionRollup
     */
    void synchronizeTopology(Snapshot snapshot, boolean removeUnspecifiedEntries) throws TopicConfigurationChangeExceptionRollup {

        synchronized (topicRegistry) {

            TopicConfigurationChangeExceptionRollup rollup = new TopicConfigurationChangeExceptionRollup();

            try {

                for(TopicConfiguration currentState : topicRegistry.entries()){

                    try {

                        TopicConfiguration mutation = locateById(snapshot, currentState.getId());

                        synchronizeTopicConfiguration(currentState, mutation, removeUnspecifiedEntries);

                    } catch (Exception topicException){

                        rollup.registerFailure(currentState.getId(), topicException);
                    }
                }

            } catch (Exception e){

                rollup.registerFailure("TopicRegistry", e);
            }

            if (rollup.hasErrors()) throw rollup;
        }
    }

    /**
     * Given two TopicConfigurations that represent the same topic, synchronize the state.
     *
     * - If the "mutation" doesn't exist and we should remove "nonspecified entries",
     *   unregister the current topic configuration.
     *
     * - If the "mutation" exists and is equal to the current state, do nothing.
     *
     * - If the "mutation" exists and is not equal to the current state, remove the current state,
     *   and register the new state.
     *
     * @param currentState
     * @param mutation
     * @param removeNonspecifiedEntries
     * @throws Exception
     */
    void synchronizeTopicConfiguration(
            TopicConfiguration currentState,
            TopicConfiguration mutation,
            boolean removeNonspecifiedEntries)
            throws Exception {

        if (mutation != null){

            if (mutation.equals(currentState)){

                logger.info("Topic '{}' is identical to configuration in snapshot...no change.",
                        mutation.getId());
            }
            else {

                this.topicRegistry.unregister(currentState.getId());

                this.topicRegistry.register(mutation);
            }
        }
        else if (removeNonspecifiedEntries) {

            topicRegistry.unregister(currentState.getId());
        }
    }

    /**
     * Get a TopicConfiguration by it's id (or null if it can't be found).
     * @param snapshot Snapshot to perform lookup on.
     * @param id ID of the Topic.
     * @return TopicConfiguration or Null.
     */
    static @Nullable TopicConfiguration locateById(Snapshot snapshot, String id){

        for (TopicConfiguration topicConfiguration : snapshot.getTopics())
            if (topicConfiguration.getId().equals(id)) return topicConfiguration;

        return null;
    }


    /**
     * Get the file that represents the latest snapshot.
     * @return Latest snapshot file path.
     */
    File getLatestFile() {

        return new File(String.format(
                "%s%s%s.%s",
                this.snapshotDirectory.getAbsolutePath(),
                File.separator,
                "snapshot.latest",
                this.serializer.serializedFileExtension()
        ));
    }

    /**
     * Get the correct file path for a Snapshot instance (a never overwritten file that
     * represents the instance).
     * @param snapshot Snapshot instance.
     * @return Correct file path for the instance file.
     */
    File getInstanceFile(Snapshot snapshot) {

        FilenameSnapshotDescriptor snapshotDescriptor =
                new FilenameSnapshotDescriptor(snapshot.getId(), snapshot.getTimestamp());

        return new File(String.format(
                "%s%s%s.%s",
                this.snapshotDirectory.getAbsolutePath(),
                File.separator,
                snapshotDescriptor.getBaseFilename(),
                this.serializer.serializedFileExtension()
        ));
    }

    /**
     * Dehydrate the latest Snapshot file.
     * @return Latest Snapshot.
     * @throws Exception Encountered if there was an issue reading or deserializing the snapshot.
     */
    Snapshot dehydrateLatestSnapshot() throws Exception {

        return dehydrateSnapshot(getLatestFile());
    }

    /**
     * Dehydrate a serialized snapshot from the filesystem.
     * @param snapshotFile Snapshot file to dehydrate.
     * @return Snapshot instance.
     * @throws Exception thrown if an error occurs attempting to read or deserialize
     * the snapshot.
     */
    Snapshot dehydrateSnapshot(File snapshotFile) throws Exception {

        String serializedSnapshot = null;

        if (snapshotFile.exists())
            serializedSnapshot = FileUtils.readFileToString(snapshotFile, "UTF-8");
        else
            throw new SnapshotFileNotExistException(snapshotFile);

        return serializer.deserialize(serializedSnapshot);
    }

    /**
     * Serialize the snapshot and write the file to the filesystem.
     * @param serializedSnapshot The serialized form of the snapshot.
     * @param filename name of the file to write.
     * @throws Exception any error encountered during the writing of the file.
     */
    static void writeToFileSystem(String serializedSnapshot, File filename) throws Exception {

        FileUtils.writeStringToFile(filename, serializedSnapshot, "UTF-8", false);
    }
}
