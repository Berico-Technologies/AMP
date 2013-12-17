package amp.topology.global;

import java.util.Collection;

/**
 * Container for the routes associated with a particular topology
 *
 * @author Richard Clayton (Berico Technologies)
 */
public interface TopicConfiguration {

    String getId();

    String getDescription();

    //TODO: Maybe an ACL or visibility field in the future.

    // CRUD for ProducerGroups

    void addProducerGroup(ProducerGroup<? extends  Partition> producerGroup);

    void removeProducerGroup(String id);

    void removeProducerGroup(ProducerGroup<? extends Partition> producerGroup);

    ProducerGroup<? extends Partition> getProducerGroup(String id);

    Collection<ProducerGroup<? extends Partition>> getProducerGroups();


    // CRUD for ConsumerGroups

    void addConsumerGroup(ConsumerGroup<? extends Partition> consumerGroup);

    void removeConsumerGroup(String id);

    void removeConsumerGroup(ConsumerGroup<? extends Partition> consumerGroup);

    ConsumerGroup<? extends Partition> getConsumerGroup(String id);

    Collection<ConsumerGroup<? extends Partition>> getConsumerGroups();


    // CRUD for Connectors

    void addConnector(Connector<? extends Partition, ? extends Partition> connector);

    void removeConnector(String id);

    void removeConnector(Connector<? extends Partition, ? extends Partition> connector);

    Connector<? extends Partition, ? extends Partition> getConnector(String id);

    Collection<Connector<? extends Partition, ? extends Partition>> getConnectors();


    // Listeners

    void addListener(Listener listener);

    void removeListener(Listener listener);

    public interface Listener {

        void onProducerGroupAdded(ProducerGroup<? extends Partition> producerGroup);

        void onProducerGroupRemoved(ProducerGroup<? extends Partition> producerGroup);

        void onConsumerGroupAdded(ConsumerGroup<? extends Partition> consumerGroup);

        void onConsumerGroupRemoved(ConsumerGroup<? extends Partition> consumerGroup);

        void onConnectorAdded(Connector<? extends Partition, ? extends Partition> connector);

        void onConnectorRemoved(Connector<? extends Partition, ? extends Partition> connector);
    }
}
