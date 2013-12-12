package amp.topology.resources;

import java.util.Collection;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.util.JSONPObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import amp.topology.core.Broker;
import amp.topology.core.ExtendedExchange;
import amp.topology.core.ITopologyRepository;

import com.yammer.metrics.annotation.Timed;

@Path("/exchange")
@Produces(MediaType.APPLICATION_JSON)
public class ExchangeResource {

	private static final Logger logger = LoggerFactory.getLogger(ExchangeResource.class);
	
	private ITopologyRepository topologyRepository;
	
	public ExchangeResource(ITopologyRepository topologyRepository) {
		
		this.topologyRepository = topologyRepository;
		
		logger.info("Instantiated with repo {}.", topologyRepository.getClass().getCanonicalName());
	}

	@GET
	@Path("/{id}")
    @Timed
	public Object getExchangeById(@PathParam("id") String id, @QueryParam("callback") String callback){
		
		logger.info("Getting exchange by Id: {}", id);

        ExtendedExchange exchange = topologyRepository.getExchange(id);
        if(callback != null && callback.length()>0){
            return new JSONPObject(callback, exchange);
        }
        return exchange;

	}
	
	@GET
	@Timed
	@Path("/broker/{host}")
	public Object getExchangesByBroker(@PathParam("host") String host, @QueryParam("callback") String callback){
		
		logger.info("Getting exchange by broker: {}", host);

        Collection<ExtendedExchange> exchanges = topologyRepository.getExchangesByBroker(host);
        if(callback != null && callback.length()>0){
            return new JSONPObject(callback, exchanges);
        }
        return exchanges;
	}
	
	@GET
	@Timed
	@Path("/broker/{host}/port/{port}")
	public Object getExchangesByBroker(
			@PathParam("host") String host, @PathParam("port") int port, @QueryParam("callback") String callback){
		
		logger.info("Getting exchange by broker: {}:{}", host, port);
        Collection<ExtendedExchange> exchanges = topologyRepository.getExchangesByBroker(host, port);
        if(callback != null && callback.length()>0){
            return new JSONPObject(callback, exchanges);
        }
        return exchanges;
	}
	
	@GET
	@Timed
	@Path("/broker/{host}/port/{port}/vhost/{vhost}")
	public Object getExchangesByBroker(
			@PathParam("host") String host, @PathParam("port") int port, 
			@PathParam("vhost") String vhost, @QueryParam("callback") String callback){
		
		String virtualHost = decodeSlashes(vhost);
		
		logger.info("Getting exchange by broker: {}:{}{}", new Object[]{ host, port, virtualHost });
        Collection<ExtendedExchange> exchanges = topologyRepository.getExchangesByBroker(host, port, virtualHost);
        if(callback != null && callback.length()>0){
            return new JSONPObject(callback, exchanges);
        }
        return exchanges;
	}
	
	@GET
	@Timed
	@Path("/brokers")
	public Object getBrokers(@QueryParam("callback") String callback){
		
		logger.info("Getting brokers.");
        Collection<Broker> brokers = topologyRepository.getBrokers();
        if(callback != null && callback.length()>0){
            return new JSONPObject(callback, brokers);
        }
        return brokers;
    }
	
	@GET
    @Timed
	public Object getExchanges(@QueryParam("callback") String callback){
		
		logger.info("Getting exchanges.");
        Collection<ExtendedExchange> exchanges = topologyRepository.getExchanges();
        if(callback != null && callback.length()>0){
            return new JSONPObject(callback, exchanges);
        }
        return exchanges;
	}
	
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public Object createExchange(ExtendedExchange exchange, @QueryParam("callback") String callback){
		
		logger.info("Creating exchange: {}", exchange);
		
		topologyRepository.createExchange(exchange);
		
		// JSON.parse will barf if you don't use double quotes
		String formattedString = String.format("{ \"id\": \"%s\" }", exchange.getId());

        if(callback != null && callback.length()>0){
            return new JSONPObject(callback, exchange.getId());
        }
        return formattedString;
	}
	
	@POST
	@Path("/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Timed
	public Object updateExchange(ExtendedExchange exchange, @QueryParam("callback") String callback){
		
		logger.info("Updating exchange: {}", exchange);
		
		topologyRepository.updateExchange(exchange);
		
		Response response = Response.ok().build();
        if(callback != null && callback.length()>0){
            return new JSONPObject(callback, response);
        }
        return response;

	}
	
	@DELETE
	@Path("/{id}")
    @Timed
	public Object removeExchange(@PathParam("id") String id, @QueryParam("callback") String callback){
		
		logger.info("Deleting exchange by Id: {}", id);
		
		topologyRepository.removeExchange(id);

        Response response = Response.ok().build();
        if(callback != null && callback.length()>0){
            return new JSONPObject(callback, response);
        }
        return response;
	}
	
	static String decodeSlashes(String string){
		
		return string.replace("%2F", "/");
	}
}
