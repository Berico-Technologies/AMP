package amp.topology.snapshot.impl;

import java.io.File;

/**
 * Thrown if the Snapshot File does not exist.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class SnapshotFileNotExistException extends Exception {

    private File snapshotFile;

    public SnapshotFileNotExistException(File snapshotFile) {

        super(String.format("Snapshot File '%s' does not exist.", snapshotFile.getAbsolutePath()));

        this.snapshotFile = snapshotFile;
    }

    /**
     * The offending file that does not exist.
     * @return Non-existent file.
     */
    public File getSnapshotFile() {
        return snapshotFile;
    }
}
