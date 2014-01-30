package amp.topology.global.exceptions;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class ConnectorAlreadyExistsException extends Exception {

    public ConnectorAlreadyExistsException(String topicConfigurationId, String connectorId) {

        super(
            String.format(
                "BasicTopic '%s' already contains a connector with the id '%s'.",
                topicConfigurationId,
                connectorId));
    }
}
