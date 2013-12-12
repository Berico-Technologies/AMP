package amp.topology.resources;

import java.io.IOException;
import java.util.Collection;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.util.JSONPObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.annotation.Timed;

import amp.topology.core.repo.snapshot.TopologySnapshotUtility;

@Path("/snapshot")
@Produces(MediaType.APPLICATION_JSON)
public class TopologySnapshotResource {

	private static final Logger logger = LoggerFactory.getLogger(TopologySnapshotResource.class);
	
	TopologySnapshotUtility snapshotUtility;
	
	public TopologySnapshotResource(TopologySnapshotUtility snapshotUtility){
		
		this.snapshotUtility = snapshotUtility;
	}
	
	@GET
	@Path("/format")
	@Timed
	public Object getSerializationFormat(@QueryParam("callback") String callback){
		
		logger.info("Getting snapshot format.");
        String formattedString = String.format("{ \"format\": \"%s\" }", this.snapshotUtility.getSnapshotFileFormat());
        if(callback != null && callback.length()>0){
            return new JSONPObject(callback, formattedString);
        }

		return formattedString;
	}
	
	@GET
	@Path("/files")
	@Timed
	public Object getSnapshots(@QueryParam("callback") String callback){
		
		logger.info("Getting snapshots");
        Collection<String> snapshot =this.snapshotUtility.getSnapshotFiles();
        if(callback != null && callback.length()>0){
            return new JSONPObject(callback, snapshot);
        }

		return snapshot;
	}
	
	@GET
	@Timed
	@Path("/file/{snapshot}")
	@Produces(MediaType.TEXT_PLAIN)
	public Object getSnapshot(@PathParam("snapshot") String snapshotName, @QueryParam("callback") String callback) throws IOException{
		
		logger.info("Getting snapshot with name: {}", snapshotName);
        String fileContents =this.snapshotUtility.getSnapshotFileContents(snapshotName);
        if(callback != null && callback.length()>0){
            return new JSONPObject(callback, fileContents);
        }

		return fileContents;
	}
	
	@POST
	@Timed
	@Path("/import/{snapshot}")
	public Object importSnapshot(@PathParam("snapshot") String snapshotName, @QueryParam("callback") String callback) throws IOException{
		
		logger.info("Importing snapshot: {}", snapshotName);
		
		this.snapshotUtility.importFileIntoRepository(snapshotName);
        Response response =  Response.ok().build();
        if(callback != null && callback.length()>0){
            return new JSONPObject(callback, response);
        }
        return response;

	}
	
	@POST
	@Timed
	@Path("/import-current")
	public Object importSnapshot(@QueryParam("callback") String callback) throws IOException{
		
		logger.info("Importing current snapshot.");
		
		this.snapshotUtility.importFileIntoRepository();
        Response response =  Response.ok().build();
        if(callback != null && callback.length()>0){
            return new JSONPObject(callback, response);
        }
        return response;
	}
	
	@POST
	@Timed
	@Path("/import")
	@Consumes(MediaType.TEXT_PLAIN)
	public Object importSnapshotFromString(String snapshot, @QueryParam("callback") String callback) throws IOException{
		
		logger.info("Importing snapshot from form.");
		
		this.snapshotUtility.importSerializedSnapshotIntoRepository(snapshot);
        Response response =  Response.ok().build();
        if(callback != null && callback.length()>0){
            return new JSONPObject(callback, response);
        }
        return response;
	}
	
	@POST
	@Timed
	@Path("/merge/{snapshot}")
	public Object mergeSnapshot(@PathParam("snapshot") String snapshotName, @QueryParam("callback") String callback) throws IOException{
		
		logger.info("Merging snapshot: {}", snapshotName);
		
		this.snapshotUtility.mergeFileIntoRepository(snapshotName);
        Response response =  Response.ok().build();
        if(callback != null && callback.length()>0){
            return new JSONPObject(callback, response);
        }
        return response;
	}
	
	@POST
	@Timed
	@Path("/merge-current")
	public Object mergeSnapshot(@QueryParam("callback") String callback) throws IOException{
		
		logger.info("Merging current snapshot.");
		
		this.snapshotUtility.mergeFileIntoRepository();
        Response response =  Response.ok().build();
        if(callback != null && callback.length()>0){
            return new JSONPObject(callback, response);
        }
        return response;
	}
	
	@POST
	@Timed
	@Path("/merge")
	@Consumes(MediaType.TEXT_PLAIN)
	public Object mergeSnapshotFromString(String snapshot, @QueryParam("callback") String callback) throws IOException{
		
		logger.info("Merging snapshot from form.");
		
		this.snapshotUtility.mergeSerializedSnapshotIntoRepository(snapshot);
        Response response =  Response.ok().build();
        if(callback != null && callback.length()>0){
            return new JSONPObject(callback, response);
        }
        return response;
	}
	
	
	@POST
	@Timed
	@Path("/export")
	public Object exportSnapshot(@QueryParam("callback") String callback) throws IOException{
		
		logger.info("Exporting snapshot");
		
		this.snapshotUtility.exportSnapshotToFile();
        Response response =  Response.ok().build();
        if(callback != null && callback.length()>0){
            return new JSONPObject(callback, response);
        }
        return response;
	}
}
