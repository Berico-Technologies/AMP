package amp.policy.resources;

import amp.policy.core.InterceptorProvider;
import amp.policy.core.InterceptorRegistrar;
import amp.policy.resources.model.InterceptorProviderInfo;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Richard Clayton (Berico Technologies)
 */
@Path("/interceptors")
@Produces(MediaType.APPLICATION_JSON)
public class InterceptorRegistrarResource {

    @Autowired
    private InterceptorRegistrar registrar;

    @GET
    public Collection<InterceptorProviderInfo> getInterceptorProviderInformation(){

        ArrayList<InterceptorProviderInfo> infos = Lists.newArrayList();

        for(InterceptorProvider provider : registrar.getInterceptorProviders()){

            infos.add(InterceptorProviderInfo.get(provider));
        }

        return infos;
    }
}
