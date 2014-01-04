package amp.topology.snapshot.impl.serializers;

import amp.topology.snapshot.Snapshot;
import amp.topology.snapshot.impl.SnapshotSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Serializes and Deserializes Snapshots to/from JSON.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class JsonSnapshotSerializer implements SnapshotSerializer {

    Gson gson;

    public JsonSnapshotSerializer(){

        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public String serializedFileExtension() {

        return "json";
    }

    @Override
    public String serialize(Snapshot snapshot) {

        return gson.toJson(snapshot);
    }

    @Override
    public Snapshot deserialize(String snapshot) {

        return gson.fromJson(snapshot, Snapshot.class);
    }
}
