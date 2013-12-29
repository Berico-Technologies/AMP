package amp.topology.protocols.rabbit.management;

/**
 * Thrown if an attempt to add an already existing ManagementEndpoint to a cluster.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class ManagementEndpointAlreadyExistsException extends Exception {

    public ManagementEndpointAlreadyExistsException(String clusterId, String endpointId) {

        super(String.format("Cluster '%s' already has a management endpoint with the id '%s'.", clusterId, endpointId));
    }
}
