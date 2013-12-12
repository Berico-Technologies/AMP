package amp.topology.tasks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import amp.topology.core.repo.snapshot.TopologySnapshotUtility;

import com.google.common.collect.ImmutableMultimap;
import com.yammer.dropwizard.tasks.Task;

/**
 * Import a Snapshot file, replacing the contents of
 * the existing repository with the entries in the 
 * snapshot file.
 * 
 * @author Richard Clayton (Berico Technologies)
 */
public class ImportSnapshotTask extends Task {

	public static String IMPORT_PATH = "importPath";
	
	TopologySnapshotUtility topologySnapshotUtility = null;
	
	public ImportSnapshotTask(TopologySnapshotUtility topologySnapshotUtility) {
		
		super("import-snapshot");
		
		this.topologySnapshotUtility = topologySnapshotUtility;
	}

	@Override
	public void execute(ImmutableMultimap<String, String> properties, PrintWriter writer) throws Exception {
		
		if (properties.containsKey(IMPORT_PATH)){
		
			String importPath = properties.get(IMPORT_PATH).asList().get(0);
			
			if (importPath != null){
				
				File importFile = new File(importPath);
				
				if (importFile.exists() && importFile.isFile()){
					
					topologySnapshotUtility.importFileIntoRepository(importFile);
					
				} else {
					
					throw new FileNotFoundException(
						String.format("File does not exist: %s", importPath));
				}
			}
		}
		else {
			
			topologySnapshotUtility.importFileIntoRepository();
		}
		
		writer.println("Snapshot imported.");
	}
}
