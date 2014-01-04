package amp.topology.snapshot.exceptions;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class SnapshotDoesNotExistException extends Exception {

    public SnapshotDoesNotExistException(String snapshotId) {
        super(String.format("Snapshot with id '%s' not found.", snapshotId));
    }
}
