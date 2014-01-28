package amp.topology.protocols.rabbit.topology;

import amp.rabbit.topology.ConsumingRoute;
import amp.rabbit.topology.Exchange;
import amp.rabbit.topology.Queue;
import amp.topology.protocols.rabbit.management.Cluster;
import amp.topology.protocols.rabbit.management.RmqModelAdaptors;
import amp.topology.protocols.rabbit.requirements.RabbitRouteRequirements;
import rabbitmq.mgmt.RabbitMgmtService;

import java.util.Collection;

/**
 * Represents the Consumption side of RabbitMQ-based partitions, which includes the exchange
 * you are binding to, routing keys, and Queue configuration.  By "Queue configuration" we
 * mean the essential ingredients to define a "Queue" dynamically, since we don't want to
 * have every consumer connect to the same queue by name.  This implementation requires subclasses
 * to provide the strategy for customizing the default configuration before providing the information
 * during a Route Filtering.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public abstract class RabbitConsumerPartition extends BaseRabbitPartition implements ConsumingRouteProvider {

    /**
     * Allows derived classes to customize the state of Queue before it is supplied to consumers.
     *
     * In addition to providing this mechanism to process a queue before it's provided to a requester,
     * the RabbitConsumerPartition class also provides mechanisms to create the Queue and Bind it to
     * the BasePartition's underlying exchange.
     *
     */
    public interface QueueProcessor {

        /**
         * Process/customize the queue before it's passed to the requester.
         * @param requirements Route Requirements (info that can help customize the queue).
         * @param safeToMutateQueue Queue to Mutate.
         * @throws Exception implementers may throw an exception if any error occurs during the process
         *                   of setting up a queue.
         */
        void customize(RabbitRouteRequirements requirements, Queue safeToMutateQueue) throws Exception;
    }

    /**
     * Requires subclasses define a mechanism for dynamically processing/customizing the queue before
     * the queue is passed back to the requester.  If the prototype is sufficient (e.g.
     * you don't need to dynamically configure the queue based on the requester), simply
     * provide an instance of the NoOpProcessor that will do absolutely nothing.
     * @return QueueProcessor instance.
     */
    protected abstract QueueProcessor getQueueProcessor();


    /**
     * Literally does nothing.
     */
    public static class NoOpProcessor implements QueueProcessor {

        @Override
        public void customize(RabbitRouteRequirements requirements, Queue safeToMutateQueue) {}
    }

    /**
     * Default Queue Prototype.
     */
    private Queue queuePrototype = Queue.builder()
            .declare(true)
            .isAutoDelete(false)
            .isDurable(false)
            .isExclusive(false)
            .build();

    /**
     * Initialize the partition using the default Queue configuration specified by this class.
     * @param cluster     the Cluster in which the Exchange should exist.
     * @param exchange    Exchange configuration.
     * @param routingKeys Routing Keys for producing or consuming on the exchange.
     */
    public RabbitConsumerPartition(Cluster cluster, Exchange exchange, Collection<String> routingKeys) {
        super(cluster, exchange, routingKeys);
    }

    /**
     * Initialize the partition with the Cluster, Exchange, Routing Key Info, and a Queue prototype that
     * represents that default setting for the queue when constructed for a client.
     *
     * @param cluster     the Cluster in which the Exchange should exist.
     * @param exchange    Exchange configuration.
     * @param routingKeys Routing Keys for producing or consuming on the exchange.
     * @param queuePrototype A Queue to use as a template for configuration for the BaseTopic Subscription.
     */
    public RabbitConsumerPartition(Cluster cluster, Exchange exchange, Collection<String> routingKeys, Queue queuePrototype) {
        super(cluster, exchange, routingKeys);

        this.queuePrototype = queuePrototype;
    }

    @Override
    public String getDescription() {
        //TODO: Better description
        return "Rabbit Consumer BasePartition mapped to exchange... ";
    }

    /**
     * Create a Queue on the Cluster.
     * @param queue Queue to create.
     * @throws Exception If there's a failure to create the queue, the exception will be propagated.
     */
    protected void createQueue(Queue queue) throws Exception {

        final rabbitmq.mgmt.model.Queue rmqQueue = RmqModelAdaptors.to(queue, getCluster().getVirtualHost());

        getCluster().executeManagementTask(new Cluster.ManagementTask<Void>() {
            @Override
            public Void execute(RabbitMgmtService rmq) {

                rabbitmq.mgmt.model.Queue q = rmq.queues().get(rmqQueue.getName());

                if (q == null)
                    rmq.queues().create(rmqQueue);

                return null;
            }
        });
    }

    /**
     * Creates bindings using the exchange and routing keys defined on the BasePartition.
     * @param queue Queue to bind to the Exchange using the BasePartition's routing keys.
     * @throws Exception an error that may arise interacting with the RabbitMQ cluster.
     */
    protected void createBindings(Queue queue) throws Exception {

        String virtualHost = getCluster().getVirtualHost();

        final rabbitmq.mgmt.model.Queue rmqQueue = RmqModelAdaptors.to(queue, virtualHost);
        final rabbitmq.mgmt.model.Exchange rmqExchange = RmqModelAdaptors.to(getExchange(), virtualHost);

        getCluster().executeManagementTask(new Cluster.ManagementTask<Void>() {

            @Override
            public Void execute(RabbitMgmtService rmq) {

                for (String routingKey : getRoutingKeys()){

                    rmq.bindings().create(rmqExchange, rmqQueue, routingKey);
                }

                return null;
            }
        });
    }

    /**
     * Converts the static and dynamic configuration aspects of this BasePartition into
     * a ConsumingRoute.
     * @param requirements Route Requirements that can help customize the ConsumingRoute.
     * @return ConsumingRoute
     * @throws Exception May thrown an exception if an error is encountered by the Customizer
     *         when attempting to setup of the queue.
     */
    @Override
    public ConsumingRoute getConsumingRoute(RabbitRouteRequirements requirements) throws Exception {

        Queue consumerQueue = clone(this.queuePrototype);

        getQueueProcessor().customize(requirements, consumerQueue);

        return ConsumingRoute.builder()
                .brokers(getBrokers())
                .exchange(getExchange())
                .routingkeys(getRoutingKeys())
                .queue(consumerQueue)
                .build();
    }

    /**
     * Clones queue, providing a separate object with identical state.
     * @param q Queue to clone.
     * @return A clone of the original queue.
     */
    static Queue clone(Queue q){

        return Queue.builder()
                .isAutoDelete(q.isAutoDelete())
                .isDurable(q.isDurable())
                .isExclusive(q.isExclusive())
                .declare(q.shouldDeclare())
                .name(q.getName())
                .arguments(q.getArguments())
                .build();
    }
}
