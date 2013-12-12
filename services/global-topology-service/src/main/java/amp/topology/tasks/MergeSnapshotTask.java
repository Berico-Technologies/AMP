package amp.topology.tasks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import amp.topology.core.repo.snapshot.TopologySnapshotUtility;

import com.google.common.collect.ImmutableMultimap;
import com.yammer.dropwizard.tasks.Task;

/**
 * Merge a Topology Snapshot with the existing TopologyRepository.
 * 
 * This is an additive task (it will not purge the existing state of 
 * the Topology Repository).
 * 
 * @author Richard Clayton (Berico Technologies)
 */
public class MergeSnapshotTask extends Task {

	public static String IMPORT_PATH = "importPath";
	
	TopologySnapshotUtility topologySnapshotUtility = null;
	
	public MergeSnapshotTask(TopologySnapshotUtility topologySnapshotUtility) {
		
		super("merge-snapshot");
		
		this.topologySnapshotUtility = topologySnapshotUtility;
	}

	@Override
	public void execute(ImmutableMultimap<String, String> properties, PrintWriter writer) throws Exception {
		
		if (properties.containsKey(IMPORT_PATH)){
		
			String importPath = properties.get(IMPORT_PATH).asList().get(0);
			
			if (importPath != null){
				
				File importFile = new File(importPath);
				
				if (importFile.exists() && importFile.isFile()){
					
					topologySnapshotUtility.mergeFileIntoRepository(importFile);
					
				} else {
					
					throw new FileNotFoundException(
						String.format("File does not exist: %s", importPath));
				}
			}
		}
		else {
			
			topologySnapshotUtility.mergeFileIntoRepository();
		}
		
		writer.println("Snapshot merged.");
	}

}
