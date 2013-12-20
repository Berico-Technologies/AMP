package amp.topology.global.exceptions;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class TopologyGroupNotExistException extends Exception {

    public TopologyGroupNotExistException(String topicConfigurationId, String groupId, boolean isProducerGroup) {

        super(
            String.format(
                "Topic '%s' does not contain a %s with an id of '%s'.",
                topicConfigurationId,
                (isProducerGroup)? "ProducerGroup" : "ConsumerGroup",
                groupId));
    }
}
