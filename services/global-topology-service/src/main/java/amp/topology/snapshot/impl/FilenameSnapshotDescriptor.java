package amp.topology.snapshot.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.File;

/**
 * Used to describe a snapshot without needing to deserialize it.
 *
 * Please extend the FilenameSnapshotDescriptor adding whatever metadata you would like.
 *
 * This implementation is rather simple because it is used with the FileSystemSnapshotManager
 * and only reads the filename, vice trying to deserialize the file to get other metadata.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class FilenameSnapshotDescriptor implements amp.topology.snapshot.SnapshotDescriptor {

    private static final DateTimeFormatter ISO_DATE_TIME = ISODateTimeFormat.dateTime();

    private final String id;

    private final long timestamp;

    private final String friendlyTimestamp;

    private FilenameSnapshotDescriptor(String id, long timestamp, String friendlyTimestamp) {
        this.id = id;
        this.timestamp = timestamp;
        this.friendlyTimestamp = friendlyTimestamp;
    }

    /**
     * Instantiate the object with the id and timestamp.
     * @param id Id of the Snapshot
     * @param timestamp Timestamp of the snapshot.
     */
    public FilenameSnapshotDescriptor(String id, long timestamp) {
        this.id = id;
        this.timestamp = timestamp;
        this.friendlyTimestamp = new DateTime(timestamp).toString(ISO_DATE_TIME);
    }

    /**
     * Id of the Snapshot.
     * @return Id of the Snapshot.
     */
    @Override
    public String getId() {

        return id;
    }

    /**
     * Time in milliseconds when the snapshot was taken.
     * @return time in milliseconds.
     */
    @Override
    public long getTimestamp() {

        return timestamp;
    }

    /**
     * The formatted timestamp that's easier to read than a UNIX epoch.
     * @return Friendly timestamp.
     */
    public String getFriendlyTimestamp() {

        return friendlyTimestamp;
    }

    /**
     * This is used to compose the base filename (i.e. without base path and extension) of the instance
     * snapshot file.
     * @return Base filename
     */
    public String getBaseFilename(){

        return String.format("snapshot.%s.%s", this.id, this.friendlyTimestamp);
    }


    /**
     * Convert a filename to a Snapshot descriptor.
     * @param snapshotFile File descripter to interpret into a Snapshot Descriptor.
     * @return Snapshot Descriptor.
     */
    public static FilenameSnapshotDescriptor fromFile(File snapshotFile) {

        String filename = snapshotFile.getName();

        Iterable<String> nameParts = Splitter.on('.').split(filename);

        int position = 0;

        String id = null;
        String friendlyTimestamp = null;

        for(String part : nameParts){

            position++;

            if (position == 2)
                id = part;
            if (position == 3)
                friendlyTimestamp = part;
        }

        Preconditions.checkNotNull(id);
        Preconditions.checkNotNull(friendlyTimestamp);

        return new FilenameSnapshotDescriptor(id, new DateTime(friendlyTimestamp).getMillis(), friendlyTimestamp);
    }
}
