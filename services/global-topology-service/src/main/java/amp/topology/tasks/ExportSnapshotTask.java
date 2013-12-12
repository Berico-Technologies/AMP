package amp.topology.tasks;

import java.io.PrintWriter;

import amp.topology.core.repo.snapshot.TopologySnapshotUtility;

import com.google.common.collect.ImmutableMultimap;
import com.yammer.dropwizard.tasks.Task;

/**
 * Export the active entries in the repository to the file system.
 * 
 * @author Richard Clayton (Berico Technologies)
 */
public class ExportSnapshotTask extends Task {

	TopologySnapshotUtility topologySnapshotUtility;
	
	public ExportSnapshotTask(TopologySnapshotUtility topologySnapshotUtility) {
		
		super("export-snapshot");
		
		this.topologySnapshotUtility = topologySnapshotUtility;
	}

	@Override
	public void execute(
		ImmutableMultimap<String, String> queryStringArguments, 
		PrintWriter writer)
			throws Exception {
		
		this.topologySnapshotUtility.exportSnapshotToFile();
		
		writer.print("Snapshot completed.");
	}
}