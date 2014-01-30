package amp.topology.global.impl;

import amp.topology.global.Connector;
import amp.topology.global.ConsumerGroup;
import amp.topology.global.ProducerGroup;
import amp.topology.global.lifecycle.LifeCycleObserver;
import amp.topology.global.persistence.PersistenceManager;
import com.yammer.metrics.annotation.Timed;

import java.util.Map;
import java.util.UUID;

/**
 *
 *
 * @author Richard Clayton (Berico Technologies)
 */
public abstract class BaseConnector<PPART extends BasePartition, CPART extends BasePartition>
        extends BaseTopologyItem<BaseConnector.DehydratedState>
        implements Connector<PPART,CPART> {

    private String connectorId;

    private ConnectorStates connectorState = ConnectorStates.NONEXISTENT;

    private ProducerGroup<PPART> producerGroup;

    private ConsumerGroup<CPART> consumerGroup;

    /**
     * For testing purposes only.
     */
    public BaseConnector(){}

    public BaseConnector(
            String topicId,
            String description,
            ProducerGroup<PPART> producerGroup,
            ConsumerGroup<CPART> consumerGroup) {

        this(topicId, UUID.randomUUID().toString(), description, producerGroup, consumerGroup);
    }

    public BaseConnector(
            String topicId,
            String connectorId,
            String description,
            ProducerGroup<PPART> producerGroup,
            ConsumerGroup<CPART> consumerGroup) {

        setTopicId(topicId);
        setDescription(description);
        this.connectorId = connectorId;
        this.producerGroup = producerGroup;
        this.consumerGroup = consumerGroup;
    }

    /**
     * Set the Id of the BaseConnector.
     * @param id Id of the BaseConnector.
     */
    @Override
    public void setConnectorId(String id) {

        this.connectorId = id;
    }

    /**
     * Get the Id of the BaseConnector.
     * @return
     */
    @Override
    public String getConnectorId() {

        return this.connectorId;
    }

    /**
     * Set the ProducerGroup.
     * @param producerGroup ProducerGroup.
     */
    @Override
    public void setProducerGroup(ProducerGroup<PPART> producerGroup) {

        this.producerGroup = producerGroup;
    }

    /**
     * Get the ProducerGroup for this BaseConnector
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
    @Override
    public void setConsumerGroup(ConsumerGroup<CPART> consumerGroup) {

        this.consumerGroup = consumerGroup;
    }

    /**
     * Get the ConsumerGroup for this BaseConnector
     * @return ConsumerGroup
     */
    @Override
    public ConsumerGroup getConsumerGroup() {

        return this.consumerGroup;
    }

    /**
     * Set the state of the BaseConnector.  This is how derived classes set the BaseConnector state
     * and notified listeners.
     * @param newState New BaseConnector State
     */
    @Timed
    protected void setState(ConnectorStates newState, String reasonForChange) {

        ConnectorStates oldState = this.connectorState;

        this.connectorState = newState;

        LifeCycleObserver.fireOnStateChanged(this, oldState, newState, reasonForChange);
    }

    /**
     * Get the latest state of the BaseConnector.
     * @return State of the BaseConnector.
     */
    @Override
    public ConnectorStates getState() {

        return this.connectorState;
    }

    @Override
    public void save(){

        save(true);
    }

    @Override
    public void save(boolean saveAggregates) {

        if (saveAggregates) {

            this.producerGroup.save();

            this.consumerGroup.save();
        }

        LifeCycleObserver.fireOnSaved(this);
    }

    @Override
    public DehydratedState dehydrate() {

        DehydratedState state =
                new DehydratedState(
                        getClass(),
                        getTopicId(),
                        getConnectorId(),
                        getDescription(),
                        getProducerGroup().getGroupId(),
                        getConsumerGroup().getGroupId(),
                        getExtensionProperties());

        state.getExtensionProperties().putAll(getExtensionProperties());

        return state;
    }

    @Override
    public void restore(DehydratedState state) {

        setConnectorId(state.getConnectorId());

        super.restore(state);
    }

    /**
     * Mandatory state properties for a BaseConnector.
     */
    public static class DehydratedState extends TopologyState {

        private String connectorId;

        private String producerGroupId;

        private String consumerGroupId;

        public DehydratedState(
                Class<? extends Connector> connectorType,
                String topicId,
                String connectorId,
                String description,
                String producerGroupId,
                String consumerGroupId,
                Map<String, String> extensionProperties) {

            super(connectorType, topicId, description);

            this.connectorId = connectorId;
            this.producerGroupId = producerGroupId;
            this.consumerGroupId = consumerGroupId;
        }

        public String getConnectorId() {
            return connectorId;
        }

        public String getProducerGroupId() {
            return producerGroupId;
        }

        public String getConsumerGroupId() {
            return consumerGroupId;
        }
    }
}
