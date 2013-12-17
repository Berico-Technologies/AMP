package amp.topology.global.query;

import java.util.HashMap;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class RouteRequirements extends HashMap<String, String> {



    public String getProtocol(){

        return "AMQP";
    }

}
