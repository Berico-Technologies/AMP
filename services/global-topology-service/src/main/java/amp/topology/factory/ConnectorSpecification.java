package amp.topology.factory;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public interface ConnectorSpecification extends CommonSpecification {

    String getConnectorId();

    String getProducerGroupId();

    String getConsumerGroupId();
}
