package amp.topology.protocols.rabbit.requirements;

import amp.topology.protocols.common.Versioned;
import amp.topology.protocols.common.VersionedMessageBodyReader;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import javax.ws.rs.core.MultivaluedMap;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Converts the JSON representation of a Map<String, String> into a RabbitRouteRequirements object.
 *
 * This works for AMPere 3.3.0 Clients.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class RabbitRouteRequirements_3_3_0_Adaptor
        implements VersionedMessageBodyReader.VersionAdaptor<RabbitRouteRequirements> {

    private static final Type HASHMAP_S_S_TYPE_TOKEN = new TypeToken<Map<String, String>>(){}.getType();

    Gson gson = new Gson();

    /**
     * At the moment, this is the only implementation.
     * @param version Version to adapt
     * @return ALWAYS RETURNS TRUE.
     */
    @Override
    public boolean canAdapt(Versioned version) {

        return true;
    }

    /**
     * Converts a JSON Map<String, String> into a RabbitRouteRequirements object.
     * @param headers Headers Request Headers.
     * @param body Body Request Body.
     * @return RabbitRouteRequirements.
     */
    @Override
    public RabbitRouteRequirements adapt(MultivaluedMap<String, String> headers, InputStream body) {

        InputStreamReader reader = new InputStreamReader(body);

        Map<String, String> context = gson.fromJson(reader, HASHMAP_S_S_TYPE_TOKEN);

        return new RabbitRouteRequirements_3_3_0(context);
    }
}
