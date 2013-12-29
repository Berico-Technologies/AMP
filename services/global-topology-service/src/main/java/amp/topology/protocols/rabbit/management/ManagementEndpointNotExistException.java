package amp.topology.protocols.rabbit.management;

/**
 * Thrown if an attempt is made to reference a Management Endpoint that does not exist.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class ManagementEndpointNotExistException extends Exception {

    public ManagementEndpointNotExistException(String clusterId, String managementEndpointId) {
        super(
            String.format(
                "Cluster '%s' does not have a management endpoint with the id of '%s'",
                clusterId,
                managementEndpointId));
    }
}
