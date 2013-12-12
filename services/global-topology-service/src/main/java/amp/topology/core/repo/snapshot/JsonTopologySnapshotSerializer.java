package amp.topology.core.repo.snapshot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * JSON Serializer for Topology Snapshots using Google Gson.
 * 
 * @author Richard Clayton (Berico Technologies)
 */
public class JsonTopologySnapshotSerializer implements ITopologySnapshotSerializer {

	Gson gson = null;
	
	public JsonTopologySnapshotSerializer(){
		
		// Make that JSON pretty!
		this.gson = new GsonBuilder().setPrettyPrinting().create();
	}
	
	public JsonTopologySnapshotSerializer(Gson gson){
		
		this.gson = gson;
	}
	
	@Override
	public String serialize(TopologySnapshot snapshot) {
		
		return gson.toJson(snapshot);
	}

	@Override
	public TopologySnapshot deserialize(String serializedSnapshot) {
		
		return gson.fromJson(serializedSnapshot, TopologySnapshot.class);
	}

	@Override
	public String getExtension() {
		
		return "json";
	}
}
