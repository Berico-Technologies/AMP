package amp.topology.factory.impl;

import amp.topology.factory.ConnectorSpecification;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class NoConnectorFactoryAvailableForSpecificationException extends Exception {

    private final ConnectorSpecification offendingSpecification;

    public NoConnectorFactoryAvailableForSpecificationException(ConnectorSpecification offendingSpecification){

        super(String.format("Could not find factory for specification: %s", offendingSpecification));

        this.offendingSpecification = offendingSpecification;
    }

    public ConnectorSpecification getOffendingSpecification() {
        return offendingSpecification;
    }
}
