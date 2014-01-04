package amp.topology.snapshot.impl;

import amp.topology.snapshot.Snapshot;

/**
 * Conformant implementations should be able to serialize/deserialize
 * Snapshots to/from a String.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public interface SnapshotSerializer {

    String serializedFileExtension();

    String serialize(Snapshot snapshot);

    Snapshot deserialize(String snapshot);
}
