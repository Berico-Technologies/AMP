package amp.examples.adaptor.resources;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.security.core.userdetails.UserDetails;

import amp.messaging.EnvelopeHelper;
import amp.examples.adaptor.views.PublishView;
import cmf.bus.IEnvelopeBus;

import com.yammer.dropwizard.auth.Auth;
import com.yammer.metrics.annotation.Timed;

@Path("/publish")
@Produces(MediaType.APPLICATION_JSON)
public class PublishResource {

	IEnvelopeBus envelopeBus;
	
	public PublishResource(IEnvelopeBus envelopeBus){
		
		this.envelopeBus = envelopeBus;
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
    @Timed
	public PublishView getForm(@Auth UserDetails user){

		return new PublishView(user);
	}
	
	@POST
	@Timed
	public Response publish(
		@Auth UserDetails user, 
		@FormParam("topic") String topic, 
		@FormParam("payload") String payload) throws Exception{

		EnvelopeHelper eh = new EnvelopeHelper();
		
		eh.setSenderIdentity(user.getUsername());
		eh.setMessageTopic(topic);
		eh.setPayload(payload.getBytes());
		
		this.envelopeBus.send(eh.getEnvelope());
		
		return Response.ok().build();
	}
}