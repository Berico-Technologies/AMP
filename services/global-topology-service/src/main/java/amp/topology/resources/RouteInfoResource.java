package amp.topology.resources;

import java.util.Collection;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.util.JSONPObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import amp.topology.core.ExtendedRouteInfo;
import amp.topology.core.ITopologyRepository;

import com.yammer.metrics.annotation.Timed;


@Path("/route")
@Produces(MediaType.APPLICATION_JSON)
public class RouteInfoResource {

	private static final Logger logger = LoggerFactory.getLogger(RouteInfoResource.class);
	
	private ITopologyRepository topologyRepository;
	
	public RouteInfoResource(ITopologyRepository topologyRepository) {
		
		this.topologyRepository = topologyRepository;
		
		logger.info("Instantiated with repo {}.", topologyRepository.getClass().getCanonicalName());
	}
	
	@GET
	@Path("/{id}")
    @Timed
	public Object getRouteById(@PathParam("id") String id, @QueryParam("callback") String callback){
		
		logger.info("Getting route by Id: {}", id);
        ExtendedRouteInfo routeInfo = topologyRepository.getRoute(id);
        if(callback != null && callback.length()>0){
            return new JSONPObject(callback, routeInfo);
        }
        return routeInfo;
	}
	
	@GET
	@Path("/client/{client}")
    @Timed
	public Object getRoutesByClient(@PathParam("client") String client, @QueryParam("callback") String callback){
		
		logger.info("Getting routes by client: {}", client);

        Collection<ExtendedRouteInfo>  routes = topologyRepository.getRoutesByClient(client);
        if(callback != null && callback.length()>0){
            return new JSONPObject(callback, routes);
        }
        return routes;
	}
	
	@GET
	@Path("/clients")
    @Timed
	public Object getClients(@QueryParam("callback") String callback){
		
		logger.info("Getting all registered clients.");
        Collection<String> clients = topologyRepository.getClients();
        if(callback != null && callback.length()>0){
            return new JSONPObject(callback, clients);
        }
        return clients;
	}
    
	@GET
	@Path("/topic/{topic}")
    @Timed
	public Object getRoutesByTopic(@PathParam("topic") String topic, @QueryParam("callback") String callback){
		
		logger.info("Getting routes by topic: {}", topic);

        Collection<ExtendedRouteInfo> routes = topologyRepository.getRoutesByTopic(topic);
        if(callback != null && callback.length()>0){
            return new JSONPObject(callback, routes);
        }
        return routes;
	}
	
	@GET
	@Path("/topics")
    @Timed
	public Object getTopics(@QueryParam("callback") String callback){
		
		logger.info("Getting all registered topics.");
        Collection<String> topics =  topologyRepository.getTopics();
        if(callback != null && callback.length()>0){
            return new JSONPObject(callback, topics);
        }
        return topics;
	}
	
	@GET
    @Timed
	public Object getRoutes(@QueryParam("callback") String callback){
		
		logger.info("Getting routes");
        Collection<ExtendedRouteInfo> routes =  topologyRepository.getRoutes();
        if(callback != null && callback.length()>0){
            return new JSONPObject(callback, routes);
        }
        return routes;
	}
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public Object createRoute(ExtendedRouteInfo route, @QueryParam("callback") String callback){
		
		logger.info("Creating route: {}", route);
		
		topologyRepository.createRoute(route);

        String output = String.format("{ \"id\": \"%s\" }", route.getId());
        if(callback != null && callback.length()>0){
            return new JSONPObject(callback, output);
        }
		// JSON.parse will barf if you don't use double quotes
		return output;
	}
	
	@POST
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public Object updateRoute(ExtendedRouteInfo route, @QueryParam("callback") String callback){
		
		logger.info("Updating route: {}", route);
		
		topologyRepository.updateRoute(route);

        Response response = Response.ok().build();
        if(callback != null && callback.length()>0){
            return new JSONPObject(callback, response);
        }
        return response;
	}
	
	@DELETE
	@Path("/{id}")
    @Timed
	public Object removeRoute(@PathParam("id") String id, @QueryParam("callback") String callback){
		
		logger.info("Deleting route by Id: {}", id);
		
		topologyRepository.removeRoute(id);

        Response response = Response.ok().build();
        if(callback != null && callback.length()>0){
            return new JSONPObject(callback, response);
        }
        return response;
	}
}
