package amp.topology.snapshot;

import amp.topology.snapshot.exceptions.SnapshotDoesNotExistException;
import amp.topology.snapshot.exceptions.TopicConfigurationChangeExceptionRollup;
import com.google.common.base.Optional;
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
@Path("topology/snapshot/")
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class SnapshotResource {

    private static final Logger logger = LoggerFactory.getLogger(SnapshotResource.class);

    SnapshotManager snapshotManager;

    public SnapshotResource(SnapshotManager snapshotManager){

        this.snapshotManager = snapshotManager;
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Path("export")
    @Timed
    public Response export(
            @QueryParam("fmt") @DefaultValue("xml") String format,
            Optional<String> description){

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
    @Path("latest")
    @Timed
    public Response latest(){

        Snapshot latest;

        try {

            latest = snapshotManager.latest();

        } catch (Exception e){

            logger.error("Failed to retrieve latest snapshot.", e);

            return Response.serverError().build();
        }

        return Response.ok(latest).build();
    }

    @GET
    @Path("last-persisted")
    @Timed
    public Response lastTimePersisted(){

        return Response.ok(new LastPersisted(snapshotManager.lastPersisted())).build();
    }

    @GET
    @Path("snapshot/{id}")
    @Timed
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
    @Path("list")
    @Timed
    public Response list(){
        try {

            Collection<SnapshotDescriptor> snapshots = snapshotManager.list();

            return Response.ok(snapshots).build();

        }   catch (Exception e){

            logger.error("An error occurred trying to list the snapshots.", e);

            return Response.serverError().build();
        }
    }

    @POST
    @Path("overwrite/{id}")
    @Timed
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
    @Path("overwrite")
    @Timed
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
    @Path("merge/{id}")
    @Timed
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
    @Path("merge")
    @Timed
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
}
