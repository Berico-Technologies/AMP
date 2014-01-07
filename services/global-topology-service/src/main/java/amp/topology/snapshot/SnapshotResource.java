package amp.topology.snapshot;

import amp.topology.snapshot.exceptions.SnapshotDoesNotExistException;
import amp.topology.snapshot.exceptions.TopicConfigurationChangeExceptionRollup;
import com.google.common.base.Optional;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import com.yammer.metrics.annotation.Timed;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

/**
 * Endpoint for interacting with the SnapshotManager.
 *
 * @author Richard Clayton (Berico Technologies)
 */
@Path("topology/snapshots")
@Api(
    value = "service/topology/snapshots",
    description = "Operations to perform Snapshoting of the Global Topology."
)
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class SnapshotResource {

    private static final Logger logger = LoggerFactory.getLogger(SnapshotResource.class);

    SnapshotManager snapshotManager;

    public SnapshotResource(SnapshotManager snapshotManager){

        this.snapshotManager = snapshotManager;
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("/export")
    @ApiOperation(
        value = "Export a Snapshot of the Global Topology.",
        notes = "Snapshot the current topology and return it to the requester.",
        response = Snapshot.class,
        authorizations = "gts-snapshot-export"
    )
    @Timed
    public Response export(Optional<String> description){

        Snapshot snapshot;

        try {

             snapshot = snapshotManager.export(description.orNull());

        } catch (Exception e){

            logger.error("Failed to export snapshot.", e);

            return Response.serverError().build();
        }

        return Response.ok(snapshot).build();
    }

    @GET
    @Path("/latest")
    @Timed
    @ApiOperation(
        value = "Retrieve the Latest Snapshot.",
        notes = "Retrieves the latest snapshot or No Content if there is none.",
        response = Snapshot.class,
        authorizations = "gts-snapshot-get"
    )
    @ApiResponses({
            @ApiResponse(code = 404, message = "No 'latest' Snapshot.")
    })
    public Response latest(){

        Snapshot latest;

        try {

            latest = snapshotManager.latest();

        } catch (Exception e){

            logger.error("Failed to retrieve latest snapshot.", e);

            return Response.serverError().build();
        }

        if (latest == null)
            Response.status(Response.Status.NOT_FOUND).build();

        return Response.ok(latest).build();
    }

    @GET
    @Path("/last-persisted")
    @Timed
    @ApiOperation(
            value = "Get the last time a Snapshot was made.",
            notes = "Retrieves the last time a snapshot was persisted.",
            response = Snapshot.class,
            authorizations = "gts-snapshot-info"
    )
    public Response lastTimePersisted(){

        return Response.ok(new LastPersisted(snapshotManager.lastPersisted())).build();
    }

    @GET
    @Path("/snapshot/{id}")
    @Timed
    @ApiOperation(
            value = "Get a Snapshot by id.",
            notes = "Retrieves by it's id, or errors if the snapshot does not exist.",
            response = Snapshot.class,
            authorizations = "gts-snapshot-get"
    )
    @ApiResponses({
            @ApiResponse(code = 404, message = "No Snapshot with specified id.")
    })
    public Response get(@PathParam("id") String id){

        Snapshot target;

        try {

            target = snapshotManager.get(id);

        } catch (SnapshotDoesNotExistException notExistEx){

            return Response.status(Response.Status.NOT_FOUND).build();

        } catch (Exception e){

            logger.error("Error attempting to retrieve snapshot.", e);

            return Response.serverError().build();
        }

        return Response.ok(target).build();
    }

    @GET
    @Path("/list")
    @Timed
    @ApiOperation(
            value = "Retrieve the set of known Snapshots.",
            notes = "This does not provide the actual snapshots, but rather descriptors with information about the snapshot.",
            response = SnapshotCollection.class,
            authorizations = "gts-snapshot-list"
    )
    public Response list(){
        try {

            Collection<SnapshotDescriptor> snapshots = snapshotManager.list();

            return Response.ok(new SnapshotCollection(snapshots)).build();

        }   catch (Exception e){

            logger.error("An error occurred trying to list the snapshots.", e);

            return Response.serverError().build();
        }
    }

    @POST
    @Path("/overwrite/{id}")
    @Timed
    @ApiOperation(
            value = "Overwrite the existing Topology with the specified Snapshot.",
            notes = "Overwrite the existing Topology state with the Snapshot specified by the supplied id.",
            authorizations = "gts-snapshot-overwrite"
    )
    @ApiResponses({
            @ApiResponse(code = 404, message = "No Snapshot with specified id.")
    })
    public Response overwrite(@PathParam("id") String id){

        Snapshot target;

        try {

            target = snapshotManager.get(id);

        } catch (SnapshotDoesNotExistException notExistEx){

            return Response.status(Response.Status.NOT_FOUND).build();

        } catch (Exception e){

            logger.error("Error attempting to retrieve snapshot.", e);

            return Response.serverError().build();
        }

        return overwrite(target);
    }

    @POST
    @Path("/overwrite")
    @Timed
    @ApiOperation(
            value = "Overwrite the existing Topology with the supplied Snapshot.",
            notes = "Overwrite the existing Topology state with the Snapshot submitted with the request.",
            authorizations = "gts-snapshot-overwrite"
    )
    public Response overwrite(Snapshot snapshot){

        try {

            snapshotManager.overwrite(snapshot);

        } catch (TopicConfigurationChangeExceptionRollup e) {

            logger.error("Some errors occurred during the overwrite.", e);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }

        return Response.ok().build();
    }


    @POST
    @Path("/merge/{id}")
    @Timed
    @ApiOperation(
            value = "Merge the Snapshot with the existing topology.",
            notes = "Merges the Snapshot with the supplied id with the current topology configuration.",
            authorizations = "gts-snapshot-merge"
    )
    @ApiResponses({
            @ApiResponse(code = 404, message = "No Snapshot with specified id.")
    })
    public Response merge(@PathParam("id") String id){

        Snapshot target;

        try {

            target = snapshotManager.get(id);

        } catch (SnapshotDoesNotExistException notExistEx){

            return Response.status(Response.Status.NOT_FOUND).build();

        } catch (Exception e){

            logger.error("Error attempting to retrieve snapshot.", e);

            return Response.serverError().build();
        }

        return merge(target);
    }

    @POST
    @Path("/merge")
    @Timed
    @ApiOperation(
            value = "Merge the Snapshot with the existing topology.",
            notes = "Merges the supplied Snapshot with the current topology configuration.",
            authorizations = "gts-snapshot-merge"
    )
    public Response merge(Snapshot snapshot){

        try {

            snapshotManager.merge(snapshot);

        } catch (TopicConfigurationChangeExceptionRollup e) {

            logger.error("Some errors occurred during the merge.", e);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e).build();
        }

        return Response.ok().build();
    }


    /**
     * Last Persisted Wrapper.
     */
    public static class LastPersisted {

        private static final DateTimeFormatter ISO_TIME = ISODateTimeFormat.dateTime();

        private final long millis;

        private final String isoTime;

        public LastPersisted(long millis) {
            this.millis = millis;
            this.isoTime = new DateTime(millis).toString(ISO_TIME);
        }

        public long getMillis() {
            return millis;
        }

        public String getIsoTime() {
            return isoTime;
        }
    }

    /**
     * A Wrapper for Snapshots
     */
    public static class SnapshotCollection {

        private final Collection<SnapshotDescriptor> snapshots;

        public SnapshotCollection(Collection<SnapshotDescriptor> snapshots) {
            this.snapshots = snapshots;
        }

        public Collection<SnapshotDescriptor> getSnapshots() {
            return snapshots;
        }
    }
}
