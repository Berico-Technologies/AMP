package amp.anubis.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import amp.anubis.core.AttributedNamedToken;
import amp.anubis.core.IRabbitAccessManager;
import com.yammer.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;

@Path("/rabbitmq")
@Produces(MediaType.APPLICATION_JSON)
public class RabbitAuthorizationResource {

    private static final Logger LOG = LoggerFactory.getLogger(RabbitAuthorizationResource.class);

    private UserDetailsService _userDetailsService;
    private IRabbitAccessManager _accessManager;


    public RabbitAuthorizationResource(UserDetailsService userDetailsService, IRabbitAccessManager accessManager) {

        if (null == userDetailsService)
            throw new IllegalArgumentException("userDetailsService must not be null.");
        if (null == accessManager)
            throw new IllegalArgumentException("accessManager must not be null.");


        _userDetailsService = userDetailsService;
        _accessManager = accessManager;
    }


    @GET
    @Path("/user")
    @Timed
    public String authorizeUser(
            @QueryParam("username") String username,
            @QueryParam("password") String password) {

        LOG.info("Received request to authorize user {}.", username);

        IRabbitAccessManager.AccessLevel access = IRabbitAccessManager.AccessLevel.NONE;


        // load the user's details
        try {
            UserDetails userDetails = _userDetailsService.loadUserByUsername(username);

            // get the list of attributes
            ArrayList<String> attributes = new ArrayList<String>();
            for (GrantedAuthority authority : userDetails.getAuthorities()) {
                attributes.add(authority.getAuthority());
            }

            // create an attributed NamedToken
            AttributedNamedToken authzToken = new AttributedNamedToken(username, password, attributes);

            // and authorize the user
            access = _accessManager.authorizeUser(authzToken);
        }
        catch (UsernameNotFoundException unfe) {
            LOG.warn("Unable to authorize user '" + username + "'.", unfe);
            // do nothing: this will return AccessLevel.NONE, which is what we want
        }

        String response = this.buildResponse(access, true);
        LOG.debug("Authorization result for {}: {}", username, response);

        return response;
    }

    @GET
    @Path("/vhost")
    @Timed
    public String authorizeVHost(
            @QueryParam("username") String username,
            @QueryParam("vhost") String vhost) {

        LOG.info("Received request to authorize {} to virtual host '{}'.", username, vhost);

        IRabbitAccessManager.AccessLevel access = IRabbitAccessManager.AccessLevel.NONE;

        try {

            UserDetails userDetails = _userDetailsService.loadUserByUsername(username);
            access = _accessManager.authorizeVHost(userDetails, vhost);
        }
        catch (UsernameNotFoundException unfe) {
            LOG.debug("user {} not found.", username);
        }

        String response = this.buildResponse(access);
        LOG.debug("Authorization result for {}: {}", username, response);

        return response;
    }

    @GET
    @Path("/resource")
    @Timed
    public String authorizeResourcePath(
            @QueryParam("username") String username,
            @QueryParam("vhost") String vhost,
            @QueryParam("resource") String resource,
            @QueryParam("name") String name,
            @QueryParam("permission") String permission) {

        LOG.info("Received request to authorize {} to {} a(n) {} named {} on virtual host {}.",
                username, permission, resource, name, vhost);

        IRabbitAccessManager.AccessLevel access = IRabbitAccessManager.AccessLevel.NONE;

        try {

            UserDetails userDetails = _userDetailsService.loadUserByUsername(username);
            access = _accessManager.authorizeResource(userDetails, vhost, resource, name, permission);
        }
        catch (UsernameNotFoundException unfe) {
            LOG.debug("user {} not found.", username);
        }

        String response = this.buildResponse(access);
        LOG.debug("Authorization result for {}: {}", username, response);

        return response;
    }


    public String buildResponse(IRabbitAccessManager.AccessLevel access) {
        return this.buildResponse(access, false);
    }

    public String buildResponse(IRabbitAccessManager.AccessLevel access, boolean includeTags) {

        StringBuilder response = new StringBuilder();

        switch (access) {
            case NONE:
                response.append("deny");
                break;
            case USER:
                response.append("allow");
                break;
            case MANAGER:
                response.append("allow");
                if (includeTags)
                    response.append(" management");
                break;
            case ADMIN:
                response.append("allow");
                if (includeTags)
                    response.append(" administrator");
                break;
        }

        return response.toString();
    }
}
