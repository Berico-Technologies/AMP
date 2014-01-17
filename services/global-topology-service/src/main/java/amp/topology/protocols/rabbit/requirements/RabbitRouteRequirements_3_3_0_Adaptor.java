package amp.topology.protocols.rabbit.requirements;

import amp.topology.resources.common.JsonHashMapAdaptor;
import amp.topology.resources.common.Versioned;

import javax.ws.rs.core.MultivaluedMap;
import java.util.HashMap;

/**
 * Converts the JSON representation of a Map<String, String> into a RabbitRouteRequirements object.
 *
 * This works for AMPere 3.3.0 Clients.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class RabbitRouteRequirements_3_3_0_Adaptor extends JsonHashMapAdaptor<RabbitRouteRequirements> {

    /**
     * At the moment, this is the only implementation.
     * @param version Version to adapt
     * @return ALWAYS RETURNS TRUE.
     */
    @Override
    public boolean canAdapt(Versioned version) {

        return true;
    }

    @Override
    protected RabbitRouteRequirements convertFromMap(MultivaluedMap<String, String> headers, HashMap<String, String> map) {

        return new RabbitRouteRequirements_3_3_0(map);
    }

}
