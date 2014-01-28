package amp.topology.protocols.rabbit.management;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class ClusterDoesNotExistException extends Exception {

    private final String clusterId;

    public ClusterDoesNotExistException(String clusterId) {

        super(String.format("Cluster with id '%s' does not exist", clusterId));

        this.clusterId = clusterId;
    }

    public String getClusterId() {
        return clusterId;
    }
}
