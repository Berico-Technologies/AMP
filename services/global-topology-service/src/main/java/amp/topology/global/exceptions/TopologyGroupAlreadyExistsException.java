package amp.topology.global.exceptions;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class TopologyGroupAlreadyExistsException extends Exception {

    public TopologyGroupAlreadyExistsException(String topicConfigurationId, String groupId, boolean isProducerGroup) {

        super(
             String.format(
                 "TopicConfiguration '%s' already as a %s with an id of '%s'.",
                     topicConfigurationId,
                     (isProducerGroup)? "ProducerGroup" : "ConsumerGroup",
                     groupId));
    }
}
