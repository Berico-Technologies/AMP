package amp.topology.core.repo.snapshot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import amp.topology.core.ExtendedExchange;
import amp.topology.core.ExtendedRouteInfo;
import amp.topology.core.ITopologyRepository;


/**
 * Utility for importing and exporting snapshots of a Topology Repository.
 * This can be used in conjunction with the InMemoryTopologyRepository to 
 * prevent data loss, but is equally applicable to other topology implementations.
 * 
 * @author Richard Clayton (Berico Technologies).
 */
public class TopologySnapshotUtility {

	private static final Logger logger = LoggerFactory.getLogger(TopologySnapshotUtility.class);
	
	/**
	 * Prefix of the snapshot file's name.
	 */
	public static String FILENAME = "topology.snapshot";

	/**
	 * What to call the current snapshot (suffix to the file name).
	 */
	public static String CURRENT_MARKER = "current";

	/**
	 * Last time the Repository was persisted as a snapshot.
	 */
	long lastPersisted = -1l;

	/**
	 * Directory where snapshots are saved.
	 */
	File saveDirectory;
	
	/**
	 * Serializer to use when saving snapshots.
	 */
	ITopologySnapshotSerializer serializer;

	/**
	 * The object that maintains the active state of the topology.
	 */
	ITopologyRepository repository;

	/**
	 * Initialize the Utility with the save directory, 
	 * desired serializer, and topology repository.
	 * 
	 * @param saveDirectory Base directory of where snapshots should be saved.
	 * @param serializer Serializer to use when exporting or importing snapshots.
	 * @param repository Topology Repository.
	 */
	public TopologySnapshotUtility(
		File saveDirectory, ITopologySnapshotSerializer serializer, ITopologyRepository repository){

		this.saveDirectory = saveDirectory;
		this.serializer = serializer;
		this.repository = repository;
	}

	/**
	 * Export the current state of the Topology Repository to 
	 * the file system (located in the "Save Directory".
	 * @throws IOException
	 */
	public void exportSnapshotToFile() throws IOException{

		logger.info("Writing snapshot to file.");
		
		File rolloverFile = getFileOnSavedPath(getRolloverFilename());

		File currentFile = getFileOnSavedPath(getCurrentFilename());

		// Only persist if the current file exists.
		if (currentFile.exists()){

			// Roll the current file over.
			FileUtils.copyFile(currentFile, rolloverFile);
		}

		// Build the snapshot.
		TopologySnapshot snapshot = getSnapshot();

		// Persist
		writeToFile(snapshot, currentFile);
	}

	/**
	 * Import the current snapshot into the repository.  This will purge existing
	 * entries in the repository, restoring the snapshot.
	 * 
	 * If a failure occurs during importation, the original state will be restored.
	 * 
	 * @throws IOException
	 */
	public void importFileIntoRepository() throws IOException{
		
		importFileIntoRepository( getFileOnSavedPath( getCurrentFilename() ) );
	}
	
	/**
	 * Import a serialized snapshot into the repository.  This will purge existing
	 * entries in the repository, restoring the snapshot.
	 * 
	 * If a failure occurs during importation, the original state will be restored.
	 * 
	 * @param filename Relative path of the specific serialized snapshot to import.
	 * @throws IOException
	 */
	public void importFileIntoRepository(String filename) throws IOException {
		
		importFileIntoRepository( getFileOnSavedPath(filename) );
	}
	
	/**
	 * Import a serialized snapshot into the repository.  This will purge existing
	 * entries in the repository, restoring the snapshot.
	 * 
	 * If a failure occurs during importation, the original state will be restored.
	 * 
	 * @param fileToLoad Relative path of the specific serialized snapshot to import.
	 * @throws IOException
	 */
	public void importFileIntoRepository(File fileToLoad) throws IOException{
		
		logger.info("Importing {} into the repository", fileToLoad);
		
		TopologySnapshot justInCaseSnapshot = getSnapshot();
		
		repository.purge();
		
		try {
		
			mergeFileIntoRepository( fileToLoad );
		
		} catch (IOException ex){
			
			merge(justInCaseSnapshot);
			
			// Rethrow!
			throw new IOException(ex);
		}
	}
	
	/**
	 * Import a serialized snapshot into the repository.
	 * 
	 * @param serializedSnapshot Serialized Snapshot content to load.
	 */
	public void importSerializedSnapshotIntoRepository(String serializedSnapshot){
	
		logger.info("Importing serialized snapshot into the repository");
		
		TopologySnapshot snapshot = this.serializer.deserialize(serializedSnapshot);
		
		if (snapshot != null){
		
			repository.purge();
		
			merge(snapshot);
		}
	}
	
	/**
	 * Merge the current snapshot with the repository.
	 * This does not clear the existing state from the repository!
	 * 
	 * If you prefer to wipe the repository clean and only use the
	 * entries found in the Topology Snapshot, use the 
	 * "importFileIntoRepository" method.
	 * 
	 * @throws IOException
	 */
	public void mergeFileIntoRepository() throws IOException{
		
		mergeFileIntoRepository( getFileOnSavedPath( getCurrentFilename() ) );
	}
	
	/**
	 * Merge the file (representing a serialized snapshot) with the repository.
	 * This does not clear the existing state from the repository!
	 * 
	 * If you prefer to wipe the repository clean and only use the
	 * entries found in the Topology Snapshot, use the 
	 * "importFileIntoRepository" method.
	 * 
	 * @param filename 
	 * @throws IOException
	 */
	public void mergeFileIntoRepository(String filename) throws IOException{
		
		mergeFileIntoRepository( getFileOnSavedPath( filename ) );
	}
	
	/**
	 * Merge the file (representing a serialized snapshot) with the repository.
	 * This does not clear the existing state from the repository!
	 * 
	 * If you prefer to wipe the repository clean and only use the
	 * entries found in the Topology Snapshot, use the 
	 * "importFileIntoRepository" method.
	 * 
	 * @param fileToLoad File to merge.
	 * @throws IOException
	 */
	public void mergeFileIntoRepository(File fileToLoad) throws IOException{
		
		logger.info("Merging {} into repository.", fileToLoad);
		
		String serializedSnapshot = readSnapshotFileToString(fileToLoad);
		
		mergeSerializedSnapshotIntoRepository(serializedSnapshot);
	}
	
	/**
	 * Deserializes the snapshot and merges it into the repository.
	 * 
	 * @param serializedSnapshot Serialized version of the snapshot.
	 */
	public void mergeSerializedSnapshotIntoRepository(String serializedSnapshot) {
		
		TopologySnapshot snapshot = this.serializer.deserialize(serializedSnapshot);
		
		if (snapshot != null) {
		
			merge(snapshot);
		}
	}
	
	/**
	 * This is simply 'importFileIntoRepository', but makes more sense
	 * semantically in something like Spring when you want to use
	 * the "initialize-method" functionality.
	 * 
	 * Remember: 'import' clears the repository.  Use 'merge' if you want
	 * to add a snapshot file to an existing repository.
	 * 
	 * Unlike the 'importFileIntoRepository', this will not throw an
	 * exception, rather WARN that the operation was unsuccessful.
	 */
	public void importSnapshotOnInitialization() {
		
		logger.info("Importing current snapshot into repository.");
		
		try {
			
			this.importFileIntoRepository();
			
		} catch (IOException e) {
			
			logger.warn("Initialization of repository unsuccessful", e);
		}
	}
	
	/**
	 * This is simply 'mergeFileIntoRepository', but makes more sense
	 * semantically in something like Spring when you want to use
	 * the "initialize-method" functionality.
	 * 
	 * Remember: 'merge' does not clear the repository.  Use 'import' if 
	 * you want only the snapshot file's config in the repository.
	 * 
	 * Unlike the 'mergeFileIntoRepository', this will not throw an
	 * exception, rather WARN that the operation was unsuccessful.
	 */
	public void mergeSnapshotOnInitialization() {
		
		logger.info("Merging current snapshot into repository.");
		
		try {
			
			this.mergeFileIntoRepository();
			
		} catch (IOException e) {
			
			logger.warn("Initialization of repository unsuccessful", e);
		}
	}
	
	/**
	 * Get a Snapshot file as a string.  This implementation will only read
	 * non-hidden files in the "save directory" where snapshots are stored.
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	public String getSnapshotFileContents(String fileName) throws IOException{
		
		File f = getFileOnSavedPath(fileName);
		
		if (f.exists() 
			// This is a guard against a relative path:
			// {saveDirectory}../../../../../etc/blah/blah
			&& f.getParent().equals(this.saveDirectory.getAbsolutePath())
			&& f.canRead() 
			&& f.isFile() 
			&& !f.isHidden()){
			
			return readSnapshotFileToString(f);
		}
		
		return null;
	}
	
	/**
	 * Get all snapshots in the snapshot directory.
	 * @return Snapshots.
	 */
	public Collection<String> getSnapshotFiles(){
		
		List<String> snapshots = new ArrayList<String>();
		
		Iterator<File> snapshotIterator = 
				FileUtils.iterateFiles(
					this.saveDirectory, 
					new String[]{ this.serializer.getExtension() }, 
					false);
		
		while(snapshotIterator.hasNext()){
			
			snapshots.add(snapshotIterator.next().getName());
		}
		
		return snapshots;
	}
	
	/**
	 * Get the acceptable format for snapshots.
	 * @return Snapshot Format (e.g. xml, json)
	 */
	public String getSnapshotFileFormat(){
		
		return this.serializer.getExtension();
	}
	
	/**
	 * Write a snapshot to the file system using the provided serializer.
	 * @param snapshot Snapshot to write to file system.
	 * @param file Location on the file system where the file should be written.
	 * @throws IOException Thrown, most likely, if you got the path wrong.
	 */
	void writeToFile(TopologySnapshot snapshot, File file) throws IOException{
		
		// Serialized the snapshot.
		String serializedSnapshot = this.serializer.serialize(snapshot);

		// Overwrite the existing current file.
		FileUtils.writeStringToFile(file, serializedSnapshot, "UTF-8", false);
		
		// Reset the last persisted time.
		this.lastPersisted = snapshot.getTimestamp();
	}
	
	/**
	 * Merge a snapshot with the repository.  This method does not 
	 * purge existing entries, but rather creates the entries in the
	 * snapshot in the repository.  Repository implementations should
	 * manage conflicts by key (updating or ignoring as necessary).
	 * 
	 * @param snapshot Topology Snapshot to merge with repository.
	 */
	void merge(TopologySnapshot snapshot){
		
		for(ExtendedExchange exchange : snapshot.getExchanges()){
			
			this.repository.createExchange(exchange);
		}
		
		for(ExtendedRouteInfo routeInfo : snapshot.getRoutes()){
			
			this.repository.createRoute(routeInfo);
		}
		
		this.lastPersisted = snapshot.getTimestamp();
	}
	
	/**
	 * Reads the snapshot file and returns a String.
	 * @param fileToLoad File that represents the snapshot.
	 * @return Snapshot as string
	 * @throws IOException
	 */
	String readSnapshotFileToString(File fileToLoad) throws IOException{
		
		return FileUtils.readFileToString(fileToLoad, "UTF-8");
	}
	
	
	/**
	 * Create a new snapshot of the repository.
	 * @return a TopologySnapshot
	 */
	TopologySnapshot getSnapshot(){

		return new TopologySnapshot(
			this.repository.getExchanges(),
			this.repository.getRoutes());
	}

	/**
	 * Concatenate the filename with the Save Directory and return a
	 * File object.
	 * @param filename relative name of the file.
	 * @return File representing the absolute path within the save directory.
	 */
	File getFileOnSavedPath(String filename){

		String absoluteFilename = String.format("%s%s%s", 
			this.saveDirectory.getAbsolutePath(),
			File.separatorChar,
			filename);

		return new File(absoluteFilename);
	}
	
	/**
	 * Get the appropriate rollover filename.
	 * @return Filename for rollover file.
	 */
	String getRolloverFilename(){

		String ts = "unknown";
		
		if (this.lastPersisted != -1){
			
			DateTime dtg = new DateTime(this.lastPersisted);
			
			ts = dtg.toString("yyyy-MM-dd_HH.mm.ss");
		}
		
		return String.format("%s.%s.%s", 
				FILENAME, ts, serializer.getExtension());
	}

	/**
	 * Get the "current" filename (this is the name of the file
	 * that will hold the most recent snapshot's state.
	 * @return Name of the "current" snapshot.
	 */
	String getCurrentFilename(){

		return String.format("%s.%s.%s", 
			FILENAME, CURRENT_MARKER, serializer.getExtension());
	}
	
	/**
	 * Convenience method for an IOC to set the value of the static variable,
	 * in this case, the prefix we use for snapshots.
	 * 
	 * @param filenamePrefix Prefix for Snapshot file names.
	 */
	public void setFilenamePrefix(String filenamePrefix){
		
		FILENAME = filenamePrefix;
	}
	
	/**
	 * Convenience method for an IOC to set the value of the static variable,
	 * in this case, the marker we use to denote the current snapshot from
	 * rollover snapshots.
	 * 
	 * @param currentMarker Distinguishing name for the current snapshot.
	 */
	public void setCurrentMarker(String currentMarker){
		
		CURRENT_MARKER = currentMarker;
	}
}