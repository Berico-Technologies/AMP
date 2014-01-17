package amp.topology.protocols.webstomp.requirements;

import amp.topology.Constants;
import amp.topology.resources.common.Versioned;
import amp.topology.resources.common.VersionedMessageBodyReader;
import com.google.common.collect.Maps;

import javax.ws.rs.core.MultivaluedMap;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Converts headers into a WebStompRouteRequirements object.
 *
 * This works for AMPere 3.3.0 Clients.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class WebStompRouteRequirements_3_3_0_Adaptor implements VersionedMessageBodyReader.VersionAdaptor<WebStompRouteRequirements> {


    @Override
    public boolean canAdapt(Versioned version) {

        return true;
    }

    @Override
    public WebStompRouteRequirements adapt(MultivaluedMap<String, String> headers, InputStream body) {

        HashMap<String, String> context = Maps.newHashMap();

        //TODO: BREAKING CHANGE, NEED "direction" FROM HEADERS
        context.put(Constants.MESSAGE_DIRECTION, headers.getFirst("dir").toUpperCase());

        context.put(Constants.HEADER_PREFERRED_QUEUE_PREFIX, headers.getFirst("qp"));
        context.put(Constants.HEADER_PREFERRED_QUEUENAME, headers.getFirst("qn"));
        context.put(Constants.HEADER_REQUEST_TOPO_CREATION, headers.getFirst("c"));
        context.put(Constants.MESSAGE_TOPIC, headers.getFirst("topic"));
        context.put(Constants.MESSAGE_PATTERN, Constants.MESSAGE_PATTERN_PUBSUB);


        return new WebStompRouteRequirements_3_3_0(context);
    }

}
