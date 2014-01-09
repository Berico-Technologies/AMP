package amp.topology.support;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * TODO: Move this into Fallwizard Proper
 *
 * @author Richard Clayton (Berico Technologies)
 */
@Provider
@Component
public class AccessDeniedMapper implements ExceptionMapper<AccessDeniedException> {

    @Override
    public Response toResponse(AccessDeniedException e) {

        return Response.status(401).
                entity(e.getMessage()).
                type("text/plain").
                build();
    }
}
