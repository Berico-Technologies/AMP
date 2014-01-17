package amp.topology.protocols.rabbit.requirements;

import amp.topology.Constants;
import amp.topology.global.filtering.impl.DecoratedMapRouteRequirements;

import java.util.Map;

/**
 * AMPere 3.3.0 Route Requirements for RabbitMQ.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class RabbitRouteRequirements_3_3_0 extends DecoratedMapRouteRequirements implements RabbitRouteRequirements {

    /**
     * Initialize from a base context.
     * @param routeInfo base route context
     * @throws IllegalStateException
     */
    public RabbitRouteRequirements_3_3_0(Map<String, String> routeInfo) throws IllegalStateException {
        super(routeInfo);
    }

    /**
     * Hey, this is AMQP!
     * @return
     */
    @Override
    public String getProtocol() {

        return Constants.PROTOCOL_AMQP;
    }
}
