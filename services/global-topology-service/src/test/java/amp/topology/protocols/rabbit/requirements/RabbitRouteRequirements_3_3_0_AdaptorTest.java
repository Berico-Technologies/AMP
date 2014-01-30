package amp.topology.protocols.rabbit.requirements;

import amp.topology.Constants;
import amp.topology.global.filtering.RouteRequirements;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class RabbitRouteRequirements_3_3_0_AdaptorTest {

    static final char quote = '"';

    static String kvp(String key, String value){

        return quote + key + quote + " : " + quote + value + quote;
    }

    String createRoutingRequirements(
            String topic, String pattern, RouteRequirements.RouteDirections routeDirections){

        StringBuilder sb = new StringBuilder();

        sb.append("{ ")
          .append(kvp(Constants.MESSAGE_TOPIC, topic)).append(",")
          .append(kvp(Constants.MESSAGE_PATTERN, pattern)).append(",")
          .append(kvp(Constants.MESSAGE_DIRECTION, routeDirections.name()))
          .append(" }");

        return sb.toString();
    }

    @Test
    public void test_adapt(){

        RabbitRouteRequirements_3_3_0_Adaptor adaptor = new RabbitRouteRequirements_3_3_0_Adaptor();

        String expectedTopic = "amp.test.BasicTopic";
        String expectedPattern = Constants.MESSAGE_PATTERN_PUBSUB;
        RouteRequirements.RouteDirections expectedDirection = RouteRequirements.RouteDirections.CONSUME;

        String routeRequest = createRoutingRequirements(expectedTopic, expectedPattern, expectedDirection);

        InputStream is = new ByteArrayInputStream(routeRequest.getBytes());

        RabbitRouteRequirements requirements = adaptor.adapt(null, is);

        assertEquals(expectedTopic, requirements.getTopic());
        assertEquals(expectedPattern, requirements.getMessagePattern());
        assertEquals(expectedDirection, requirements.getRouteDirection());
        assertEquals(Constants.PROTOCOL_AMQP, requirements.getProtocol());
    }

}
