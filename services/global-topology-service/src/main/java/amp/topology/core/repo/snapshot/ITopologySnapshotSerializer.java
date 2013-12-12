package amp.topology.core.repo.snapshot;

public interface ITopologySnapshotSerializer {

	String getExtension();
	
	String serialize(TopologySnapshot snapshot);
	
	TopologySnapshot deserialize(String serializedSnapshot);
}