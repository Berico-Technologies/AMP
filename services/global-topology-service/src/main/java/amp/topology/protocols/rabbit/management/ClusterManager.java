package amp.topology.protocols.rabbit.management;

import java.util.Collection;

/**
 * Describes the requirements of a container that manages clusters.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public interface ClusterManager {

    /**
     * Add a cluster.
     * @param cluster Cluster to add.
     */
    void add(Cluster cluster) throws Exception;

    /**
     * Remove a cluster by id.
     * @param clusterId Id of the cluster to remove.
     * @throws ClusterDoesNotExistException if the cluster doesn't exist.
     * @throws Exception an exception encountered in releasing cluster resources.
     */
    void remove(String clusterId) throws Exception;

    /**
     * Get a Cluster by ID.
     * @param clusterId Id of the cluster.
     * @return Cluster with the supplied id.
     * @throws ClusterDoesNotExistException if the cluster doesn't exist.
     */
    Cluster get(String clusterId) throws ClusterDoesNotExistException;

    /**
     * Get whether the cluster exists.
     * @param clusterId Id of the cluster.
     * @return TRUE if the cluster exists, FALSE if it does not.
     */
    boolean exists(String clusterId);

    /**
     * Get all Clusters.
     * @return All of the clusters registered with the manager.
     */
    Collection<Cluster> list();


    /**
     * The cluster manager should return the next available candidate to host a piece of topology.
     *
     * //TODO: YAGNI - pass in state to help make a decision on what cluster to choose.
     *
     * @return Cluster to host topology items.
     */
    Cluster getNextCandidate();
}
