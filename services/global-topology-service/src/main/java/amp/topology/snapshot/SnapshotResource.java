package amp.topology.snapshot;

import amp.topology.snapshot.exceptions.SnapshotDoesNotExistException;
import amp.topology.snapshot.exceptions.TopicConfigurationChangeExceptionRollup;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.annotation.Timed;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;

/**
 * Endpoint for interacting with the SnapshotManager.
 *
 * @author Richard Clayton (Berico Technologies)
 */
@Path("topology/snapshots")
@Api(
    value = "service/topology/snapshots",
    description = "Operations to manage snapshots of the Global Topology."
)
@Produces({ "application/json;qs=1", "application/xml;qs=.5" })
public class SnapshotResource {

    private static final Logger logger = LoggerFactory.getLogger(SnapshotResource.class);

    SnapshotManager snapshotManager;

    /**
     * Set the SnapshotManager.
     * @param snapshotManager SnapshotManager
     */
    public void setSnapshotManager(SnapshotManager snapshotManager) {
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
    @Metered(name="export-metered")
    public Snapshot export(String description) throws Exception {

        Snapshot snapshot;

        snapshot = snapshotManager.export(description);

        return snapshot;
    }

    @GET
    @Path("/latest")
    @Timed
    //@Metered
    @ApiOperation(
        value = "Retrieve the Latest Snapshot.",
        notes = "Retrieves the latest snapshot or No Content if there is none.",
        response = Snapshot.class,
        authorizations = "gts-snapshot-get"
    )
    @ApiResponses({
            @ApiResponse(code = 404, message = "No 'latest' Snapshot.")
    })
    public Response latest() throws Exception {

        Snapshot latest;

        latest = snapshotManager.latest();

        if (latest == null)
            Response.status(Response.Status.NOT_FOUND).build();

        return Response.ok(latest).build();
    }

    @GET
    @Path("/last-persisted")
    @Timed
    //@Metered
    @ApiOperation(
            value = "Get the last time a Snapshot was made.",
            notes = "Retrieves the last time a snapshot was persisted.",
            response = Snapshot.class,
            authorizations = "gts-snapshot-info"
    )
    public LastPersisted lastTimePersisted(){

        return new LastPersisted(snapshotManager.lastPersisted());
    }

    @GET
    @Path("/snapshot/{id}")
    @Timed
    //@Metered
    @ApiOperation(
            value = "Get a Snapshot by id.",
            notes = "Retrieves by it's id, or errors if the snapshot does not exist.",
            response = Snapshot.class,
            authorizations = "gts-snapshot-get"
    )
    @ApiResponses({
            @ApiResponse(code = 404, message = "No Snapshot with specified id.")
    })
    public Response get(@PathParam("id") String id) throws Exception {

        Snapshot target;

        try {

            target = snapshotManager.get(id);

        } catch (SnapshotDoesNotExistException notExistEx){

            return Response.status(Response.Status.NOT_FOUND).build();

        }

        return Response.ok(target).build();
    }

    @GET
    @Path("/list")
    @Timed
    //@Metered
    @ApiOperation(
            value = "Retrieve the set of known Snapshots.",
            notes = "This does not provide the actual snapshots, but rather descriptors with information about the snapshot.",
            response = SnapshotCollection.class,
            authorizations = "gts-snapshot-list"
    )
    public Response list() throws Exception {

        Collection<SnapshotDescriptor> snapshots = snapshotManager.list();

        return Response.ok(new SnapshotCollection(snapshots)).build();
    }

    @POST
    @Path("/overwrite/{id}")
    @Timed
    //@Metered
    @ApiOperation(
            value = "Overwrite the existing Topology with the specified Snapshot.",
            notes = "Overwrite the existing Topology state with the Snapshot specified by the supplied id.",
            authorizations = "gts-snapshot-overwrite"
    )
    @ApiResponses({
            @ApiResponse(code = 404, message = "No Snapshot with specified id.")
    })
    public Response overwriteWithId(@PathParam("id") String id) throws Exception {

        Snapshot target;

        try {

            target = snapshotManager.get(id);

        } catch (SnapshotDoesNotExistException notExistEx){

            return Response.status(Response.Status.NOT_FOUND).build();

        }

        return overwrite(target);
    }

    @POST
    @Path("/overwrite")
    @Timed
    //@Metered
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
    //@Metered
    @ApiOperation(
            value = "Merge the Snapshot with the existing topology.",
            notes = "Merges the Snapshot with the supplied id with the current topology configuration.",
            authorizations = "gts-snapshot-merge"
    )
    @ApiResponses({
            @ApiResponse(code = 404, message = "No Snapshot with specified id.")
    })
    public Response mergeWithId(@PathParam("id") String id) throws Exception {

        Snapshot target;

        try {

            target = snapshotManager.get(id);

        } catch (SnapshotDoesNotExistException notExistEx){

            return Response.status(Response.Status.NOT_FOUND).build();

        }

        return merge(target);
    }

    @POST
    @Path("/merge")
    @Timed
    //@Metered
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
    @XmlRootElement
    public static class LastPersisted {

        private static final DateTimeFormatter ISO_TIME = ISODateTimeFormat.dateTime();

        private long millis;

        private String isoTime;

        private boolean hasSnapshot = true;

        public LastPersisted(long millis) {
            this.millis = millis;

            if (millis < 0){

                this.isoTime = null;
                this.hasSnapshot = false;
            }
            else {

                this.isoTime = new DateTime(millis).toString(ISO_TIME);
            }
        }

        public long getMillis() {
            return millis;
        }

        public String getIsoTime() {
            return isoTime;
        }

        public boolean getHasSnapshot() {
            return hasSnapshot;
        }
    }

    /**
     * A Wrapper for Snapshots
     */
    @XmlRootElement
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
