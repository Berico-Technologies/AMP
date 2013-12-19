package amp.topology.protocols.webstomp.requirements;

import amp.topology.global.Constants;
import amp.topology.global.filtering.impl.DecoratedMapRouteRequirements;

import java.util.Map;

/**
 * AMPere 3.3.0 Route Requirements for RabbitMQ's WebStomp.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class WebStompRouteRequirements_3_3_0 extends DecoratedMapRouteRequirements implements WebStompRouteRequirements {


    public WebStompRouteRequirements_3_3_0(Map<String, String> routeInfo) throws IllegalStateException {
        super(routeInfo);
    }

    @Override
    protected void validate() throws IllegalStateException {

        super.validate();

        // Custom validation for WebStomp
    }

    @Override
    public String getProtocol() {

        return Constants.PROTOCOL_WEBSTOMP;
    }

    @Override
    public boolean shouldCreateTopology() {
        return Boolean.parseBoolean(this.get(Constants.HEADER_REQUEST_TOPO_CREATION));
    }

    @Override
    public String queueName() {

        return this.get(Constants.HEADER_PREFERRED_QUEUENAME);
    }

    @Override
    public String queuePrefix() {

        return this.get(Constants.HEADER_PREFERRED_QUEUE_PREFIX);
    }
}
