package amp.topology.factory.specifications;

import amp.topology.anubis.AccessControlList;
import amp.topology.factory.ConnectorSpecification;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Specification for creating Connectors via the ConnectorFactory.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class ConnectorSpecification_3_3_0 extends CommonSpecification_3_3_0 implements ConnectorSpecification {

    private String connectorId;

    private String producerGroupId;

    private String consumerGroupId;

    protected ConnectorSpecification_3_3_0(){}

    /**
     * Initialize with the required parameters for specifying a Connector.
     *
     * @param topicId The id of the topic in which this connector should exist.
     * @param description A description of the purpose of the Connector.
     * @param accessControlList Access controls for the Connector (outside of the defaults).
     * @param configurationHints Any hints to the factory about how to create or configure the Connector.  This may
     *                           include something about the desired protocol, producing configuration, whatever.
     *                           The key-value pairs will be specific to the underlying implementation of the
     *                           TopicFactory and any delegated factories it may use.
     * @param connectorId The ID of the connector, which must be unique within a topic.
     * @param producerGroupId ID of the producer group to route messages from.
     * @param consumerGroupId ID of the consumer group to route messages to.
     */
    public ConnectorSpecification_3_3_0(
            String topicId,
            @Nullable String description,
            @Nullable AccessControlList accessControlList,
            @Nullable Map<String, Object> configurationHints,
            String connectorId,
            String producerGroupId,
            String consumerGroupId) {

        super(topicId, description, accessControlList, configurationHints);
        this.connectorId = connectorId;
        this.producerGroupId = producerGroupId;
        this.consumerGroupId = consumerGroupId;
    }

    /**
     * The ID of the connector.
     * @return ID of the connector.
     */
    @Override
    public String getConnectorId() {
        return connectorId;
    }

    /**
     * Get the ID of the producer group.
     * @return Producer Group Id.
     */
    @Override
    public String getProducerGroupId() {
        return producerGroupId;
    }

    /**
     * Get the ID of the consumer group.
     * @return Consumer Group Id.
     */
    @Override
    public String getConsumerGroupId() {
        return consumerGroupId;
    }


    /**
     * Get a builder for the GroupSpecification_3_3_0.
     * @return Group Specification Builder.
     */
    public ConnectorSpecificationBuilder builder(){

        return new ConnectorSpecificationBuilder();
    }

    /**
     * A Builder for the GroupSpecification_3_3_0 class.
     */
    public static class ConnectorSpecificationBuilder
            extends CommonSpecificationBuilder<ConnectorSpecification_3_3_0, ConnectorSpecificationBuilder> {

        protected ConnectorSpecificationBuilder() {  super(new ConnectorSpecification_3_3_0()); }

        /**
         * Set the id of the connector.  This must be unique within the topic.
         * @param id Desired id of the connector.
         * @return this.
         */
        public ConnectorSpecificationBuilder connectorId(String id){

            this.objectUnderConstruction.connectorId = id;

            return self();
        }

        /**
         * Set the producer group to connect to.
         * @param id Id of the producer group.
         * @return this.
         */
        public ConnectorSpecificationBuilder producerGroup(String id){

            this.objectUnderConstruction.producerGroupId = id;

            return self();
        }

        /**
         * Set the consumer group to connect to.
         * @param id Id of the consumer group.
         * @return this.
         */
        public ConnectorSpecificationBuilder consumerGroup(String id){

            this.objectUnderConstruction.consumerGroupId = id;

            return self();
        }
    }
}
