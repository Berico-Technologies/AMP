package amp.topology.factory;

import amp.topology.global.Connector;
import amp.topology.global.Partition;

/**
 * Encapsulates the construction of connectors, hiding as much detail as possible from the requester.
 *
 * The ConnectorFactory not only creates the connector, but also registers the connector with the correct BasicTopic.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public interface ConnectorFactory {

    /**
     * Create a BaseConnector with the provided specification.
     *
     * You are returned a copy of the BaseConnector so you can readily interact with it.  The BaseConnector
     * will automatically be registered with the TopicConfiguration (you don't need to do anything else).
     *
     * @param specification Specification of the BaseConnector.
     * @return ConnectorSpecification_3_3_0
     * @throws Exception an error occurring during the construction or registration process.
     */
    Connector<? extends Partition, ? extends Partition> create(ConnectorSpecification specification) throws Exception;

    /**
     * Modify the provided connector with the state represented by the specification.
     *
     * @param specification Specification of the BaseConnector (representing the mutations).
     * @return a collection of modification entries describing the results of the operation.
     * @throws Exception Any error that occurred.
     */
    Modifications modify(ConnectorSpecification specification) throws Exception;
}
