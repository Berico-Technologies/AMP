package amp.topology.protocols.rabbit.topology;

import amp.rabbit.topology.Broker;
import amp.rabbit.topology.Exchange;
import amp.topology.global.impl.BasePartition;
import amp.topology.protocols.rabbit.management.Cluster;
import amp.topology.protocols.rabbit.management.RmqModelAdaptors;
import amp.topology.protocols.rabbit.topology.exceptions.ExchangeDoesNotExistException;
import com.google.common.collect.Sets;
import rabbitmq.mgmt.RabbitMgmtService;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Represents the basic configuration for a BasePartition in RabbitMQ.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public abstract class BaseRabbitPartition extends BasePartition {

    private Cluster cluster;

    private Exchange exchange;

    private Set<String> routingKeys = Sets.newCopyOnWriteArraySet();

    /**
     * Initialize the partition with the Cluster, Exchange, and Routing Key Info.
     *
     * @param cluster the Cluster in which the Exchange should exist.
     * @param exchange Exchange configuration.
     * @param routingKeys Routing Keys for producing or consuming on the exchange.
     */
    public BaseRabbitPartition(Cluster cluster, Exchange exchange, Collection<String> routingKeys) {
        this.cluster = cluster;
        this.exchange = exchange;
        this.routingKeys.addAll(routingKeys);
    }

    /**
     * Get the Cluster on which the Exchange resides.
     * @return Cluster
     */
    public Cluster getCluster() {
        return cluster;
    }

    /**
     * Get the available Brokers to communicate with on the Cluster.
     * @return Brokers to communicate with.
     */
    public Collection<Broker> getBrokers(){

        return Collections.unmodifiableCollection(this.cluster);
    }

    /**
     * Get the Exchange represented by this BasePartition
     * @return Exchange
     */
    public Exchange getExchange() {
        return exchange;
    }

    /**
     * Get an unmodifiable Collection of Routing Keys.
     * @return unmodifiable Collection of Routing Keys.
     */
    public Collection<String> getRoutingKeys(){

        return Collections.unmodifiableCollection(routingKeys);
    }

    /**
     * Add a routing key.
     * @param routingKey Routing Key.
     */
    public void addRoutingKey(String routingKey){

        this.routingKeys.add(routingKey);
    }

    /**
     * Remove a routing key from the BasePartition.
     * @param routingKey Routing Key to remove
     * @return TRUE if a key was removed (if it actually existed).
     */
    public boolean removeRoutingKey(String routingKey){

        return this.routingKeys.remove(routingKey);
    }

    /**
     * Creates the exchange.
     * @throws Exception if the operation fails to succeed.
     */
    @Override
    public void setup() throws Exception {

        setState(PartitionStates.ACTIVATING, "Attempting to create exchange.");

        final rabbitmq.mgmt.model.Exchange ex = RmqModelAdaptors.to(exchange, cluster.getVirtualHost());

        cluster.executeManagementTask(new Cluster.ManagementTask<Void>() {

            @Override
            public Void execute(RabbitMgmtService rmq) {

                rmq.exchanges().create(ex);

                return null;
            }
        });

        setState(PartitionStates.ACTIVE, "Exchange created.");
    }

    /**
     * Removes the exchange.
     * @throws Exception if the operation fails to succeed.
     */
    @Override
    public void cleanup() throws Exception {

        setState(PartitionStates.DEACTIVATING, "Attempting to remove exchange.");

        final String exchangeName = exchange.getName();

        cluster.executeManagementTask(new Cluster.ManagementTask<Void>() {

            @Override
            public Void execute(RabbitMgmtService rmq) {

                rmq.exchanges().delete(cluster.getVirtualHost(), exchangeName);

                return null;
            }
        });

        setState(PartitionStates.NONEXISTENT, "Exchange removed, cleaning up partition.");
    }

    /**
     * Verify the state of the exchange.
     * @throws Exception Occurs if the validation check fails.
     */
    @Override
    public void verify() throws Exception {

        final String exchangeName = exchange.getName();

        boolean hasExchange = cluster.executeManagementTask(new Cluster.ManagementTask<Boolean>() {

            @Override
            public Boolean execute(RabbitMgmtService rmq) {

                rabbitmq.mgmt.model.Exchange onClusterEx = rmq.exchanges().get(cluster.getVirtualHost(), exchangeName);

                return onClusterEx != null;
            }
        });

        if (!hasExchange){

            setState(PartitionStates.IN_ERROR, "Exchange does not exist.");

            throw new ExchangeDoesNotExistException(exchange);
        }
        else {

            setState(PartitionStates.ACTIVE, "Exchange available.");
        }
    }
}
