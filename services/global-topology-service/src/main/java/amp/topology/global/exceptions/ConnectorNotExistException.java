package amp.topology.global.exceptions;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class ConnectorNotExistException extends Exception {

    /**
     * Initialize the Exception
     * @param topicConfigurationId Specific Topic Configuration in which the error was encountered
     * @param connectorId The erroneous ID of a non-existent connector.
     */
    public ConnectorNotExistException(String topicConfigurationId, String connectorId) {

        super(
            String.format(
                "Topic '%s' does not contain a connector with the id '%s'",
                topicConfigurationId,
                connectorId));
    }
}
