package amp.topology.global.impl;

import amp.topology.global.Connector;
import amp.topology.global.ConsumerGroup;
import amp.topology.global.Partition;
import amp.topology.global.ProducerGroup;
import com.google.common.collect.Sets;
import com.yammer.metrics.annotation.Timed;

import java.util.Set;
import java.util.UUID;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public abstract class BaseConnector<PPART extends Partition, CPART extends Partition> implements Connector {

    private String id;

    private String description = "";

    private ConnectorStates connectorState = ConnectorStates.NONEXISTENT;

    private ProducerGroup<PPART> producerGroup;

    private ConsumerGroup<CPART> consumerGroup;

    private Set<Listener> listeners = Sets.newCopyOnWriteArraySet();

    public BaseConnector(String description, ProducerGroup<PPART> producerGroup, ConsumerGroup<CPART> consumerGroup) {

        this(UUID.randomUUID().toString(), description, producerGroup, consumerGroup);
    }

    public BaseConnector(String id, String description, ProducerGroup<PPART> producerGroup, ConsumerGroup<CPART> consumerGroup) {
        this.id = id;
        this.description = description;
        this.producerGroup = producerGroup;
        this.consumerGroup = consumerGroup;
    }

    /**
     * Set the Id of the Connector.
     * @param id Id of the Connector.
     */
    protected void setId(String id) {

        this.id = id;
    }

    /**
     * Get the Id of the Connector.
     * @return
     */
    @Override
    public String getId() {

        return this.id;
    }

    /**
     * Set a friendly description of this Connector.
     * @param description Friendly description.
     */
    @Override
    public void setDescription(String description) {

        this.description = description;
    }

    /**
     * Get a friendly description of this connector.
     * @return Friendly description.
     */
    @Override
    public String getDescription() {

        return this.description;
    }

    /**
     * Set the ProducerGroup.
     * @param producerGroup ProducerGroup.
     */
    protected void setProducerGroup(ProducerGroup<PPART> producerGroup) {

        this.producerGroup = producerGroup;
    }

    /**
     * Get the ProducerGroup for this Connector
     * @return ProducerGroup
     */
    @Override
    public ProducerGroup getProducerGroup() {

        return this.producerGroup;
    }

    /**
     * Set the ConsumerGroup.
     * @param consumerGroup ConsumerGroup.
     */
    public void setConsumerGroup(ConsumerGroup<CPART> consumerGroup) {

        this.consumerGroup = consumerGroup;
    }

    /**
     * Get the ConsumerGroup for this Connector
     * @return ConsumerGroup
     */
    @Override
    public ConsumerGroup getConsumerGroup() {

        return this.consumerGroup;
    }

    /**
     * Set the state of the Connector.  This is how derived classes set the Connector state
     * and notified listeners.
     * @param newState New Connector State
     */
    @Timed
    protected void setState(ConnectorStates newState, String reasonForChange) {

        ConnectorStates oldState = this.connectorState;

        this.connectorState = newState;

        for (Listener listener : listeners)
            if (listener != null) listener.onConnectorStateChange(this, oldState, newState, reasonForChange);
    }

    /**
     * Get the latest state of the Connector.
     * @return State of the Connector.
     */
    @Override
    public ConnectorStates getState() {

        return this.connectorState;
    }

    /**
     * Add a listener to the connector.  If the listener is null, or already registered, this call will be
     * ignored.
     * @param listener Listener to add.
     */
    @Override
    public void addListener(Listener listener) {

        if (listener != null) listeners.add(listener);
    }

    /**
     * Remove a listener from the Connector.  If the listener is null, or not contained in the set of listeners,
     * this call will be ignored.
     * @param listener Listener to cleanup.
     */
    @Override
    public void removeListener(Listener listener) {

        if (listener != null) listeners.remove(listener);
    }
}
