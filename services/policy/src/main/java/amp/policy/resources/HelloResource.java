package amp.policy.resources;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hello")
@Produces(MediaType.APPLICATION_JSON)
public class HelloResource {

    @GET
    @Path("/{name}")
    public Object hello(@PathParam("name") String name){

        return "{ 'hello': '" + name + "' }";
    }


}
