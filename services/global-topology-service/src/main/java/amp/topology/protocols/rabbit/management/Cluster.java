package amp.topology.protocols.rabbit.management;

import amp.rabbit.topology.Broker;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import rabbitmq.mgmt.RabbitMgmtService;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Represents a group of clustered RabbitMQ Brokers
 * on the same Virtual Host.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class Cluster extends CopyOnWriteArraySet<Broker> {

    private String clusterName;

    private String virtualHost = "/";

    private Object managementEndpointsLock = new Object();

    private Set<ManagementEndpoint> managementEndpoints = Sets.newCopyOnWriteArraySet();

    /**
     * Provide the Name of the Cluster, using the default virtual host ("/").
     * @param clusterName Name of the Cluster
     */
    public Cluster(String clusterName){

        this.clusterName = clusterName;
    }

    /**
     * Provide the name of the cluster and the virtual host to use.
     * @param clusterName Name of the Cluster
     * @param virtualHost Name of the Virtual Host
     */
    public Cluster(String clusterName, String virtualHost){

        this.clusterName = clusterName;
        this.virtualHost = virtualHost;
    }

    /**
     * Add a Broker to the Cluster
     * @param broker Broker to add.
     * @return TRUE if added
     */
    @Override
    public boolean add(Broker broker){

        if (validate(broker))
            return super.add(broker);

        return false;
    }

    /**
     * Validate that the Broker configuration is valid.
     * @param broker Broker to validate
     * @return
     */
    boolean validate(Broker broker) {

        if (!broker.getVirtualHost().equals(this.virtualHost)) return false;

        return true;
    }

    /**
     * Add a Broker to the cluster.
     * @param hostname Hostname of the broker
     * @param port Port of the broker
     * @return TRUE if added
     */
    public boolean add(String hostname, int port){

        Broker broker = Broker.builder()
                .cluster(this.clusterName)
                .vhost(this.virtualHost)
                .host(hostname)
                .port(port)
                .build();

        return this.add(broker);
    }

    /**
     * Add a Broker to the cluster.
     * @param hostname Hostname of the broker
     * @param port Port of the broker
     * @param connectionStrategy Connection Strategy to use at the client.
     * @return
     */
    public boolean add(String hostname, int port, String connectionStrategy){

        Broker broker = Broker.builder()
                .cluster(this.clusterName)
                .vhost(this.virtualHost)
                .host(hostname)
                .port(port)
                .connectionStrategy(connectionStrategy)
                .build();

        return this.add(broker);
    }

    /**
     * Get the ID of the cluster.
     * @return ID of the Cluster
     */
    public String getClusterId(){

        return String.format("%s+%s", clusterName, virtualHost);
    }

    /**
     * Get the Name of the Cluster.
     * @return Name of the Cluster.
     */
    public String getClusterName() {

        return clusterName;
    }

    /**
     * Get the Virtual Host.
     * @return Virtual Host.
     */
    public String getVirtualHost() {

        return virtualHost;
    }

    /**
     * Retrieve an unmodifiable collection of Management Endpoints.
     * @return A collection of endpoints.
     */
    public Collection<ManagementEndpoint> getManagementEndpoints(){

        return Collections.unmodifiableCollection(this.managementEndpoints);
    }

    /**
     * Add a Management Endpoint to the cluster.
     * @param endpoint Endpoint to add.
     * @throws ManagementEndpointAlreadyExistsException Thrown if an endpoint exists with the same id.
     */
    public void addManagementEndpoint(ManagementEndpoint endpoint) throws ManagementEndpointAlreadyExistsException {

        synchronized (managementEndpointsLock){

            for(ManagementEndpoint mgmtEndpoint : managementEndpoints)
                if (endpoint.getId().equals(mgmtEndpoint.getId()))
                    throw new ManagementEndpointAlreadyExistsException(this.getClusterId(), endpoint.getId());

            managementEndpoints.add(endpoint);
        }
    }

    /**
     * Remove an endpoint by ID.
     * @param id ID of the endpoint to remove.
     * @throws ManagementEndpointNotExistException thrown if the endpoint with that ID does not exist.
     */
    public void removeManagementEndpoint(String id) throws ManagementEndpointNotExistException {

        synchronized (managementEndpointsLock){

            for (ManagementEndpoint mgmtEndpoint : managementEndpoints)
                if (mgmtEndpoint.getId().equals(id)){

                    managementEndpoints.remove(mgmtEndpoint);

                    return;
                }

            throw new ManagementEndpointNotExistException(this.getClusterId(), id);
        }
    }

    /**
     * Defines the interface for creating a Management Task that will be executed against the
     * RabbitMQ Cluster Management Endpoints.
     * @param <RETURN> Whatever you want out of the task.
     */
    public interface ManagementTask<RETURN> {

        /**
         * Given a configured Management Service, do whatever you need to
         * and return whatever value you want.
         * @param rmq RabbitMQ Management Service API.
         * @return The value you desire.
         */
        RETURN execute(RabbitMgmtService rmq);
    }

    /**
     * Executes a Management Task in against Management Endpoints, failing if the task
     * does not succeed on at least one endpoint.  It's assumed that the endpoints
     * belong to a single RabbitMQ cluster.  So any changes made on one endpoint should
     * affect the entire cluster.
     * @param taskToExecute Task to execute.
     * @param <RETURN> Desired Return type (defined by the Task).
     * @return Whatever it is you wanted back.
     * @throws ManagementTaskFailedOnAllEndpointsException thrown if all endpoints fail to
     * execute the task.
     */
    public <RETURN> RETURN executeManagementTask(ManagementTask<RETURN> taskToExecute)
            throws ManagementTaskFailedOnAllEndpointsException {

        Map<String, Exception> exceptionMap = Maps.newHashMap();

        for (ManagementEndpoint me : this.managementEndpoints){

            RabbitMgmtService rms = me.getManagementService();

            try {

                RETURN retValue = taskToExecute.execute(rms);

                return retValue;

            } catch (Exception e){

                exceptionMap.put(me.getId(), e);
            }
        }

        throw new ManagementTaskFailedOnAllEndpointsException(exceptionMap);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Cluster brokers = (Cluster) o;

        if (!clusterName.equals(brokers.clusterName)) return false;
        if (!virtualHost.equals(brokers.virtualHost)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + clusterName.hashCode();
        result = 31 * result + virtualHost.hashCode();
        return result;
    }
}
