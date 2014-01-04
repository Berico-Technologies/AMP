package amp.topology.snapshot;

/**
 * Represents information about an individual snapshot.  The descriptor is used to
 * return information about a snapshot without having to perform the retrieval and
 * serialization/deserialization necessary to represent the state of topology.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public interface SnapshotDescriptor {

    String getId();

    long getTimestamp();
}
