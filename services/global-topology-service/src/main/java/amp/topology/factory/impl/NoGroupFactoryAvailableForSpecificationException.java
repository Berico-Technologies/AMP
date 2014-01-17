package amp.topology.factory.impl;

import amp.topology.factory.GroupSpecification;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class NoGroupFactoryAvailableForSpecificationException extends Exception {

    private final GroupSpecification offendingSpecification;

    public NoGroupFactoryAvailableForSpecificationException(GroupSpecification offendingSpecification){

        super(String.format("Could not find factory for specification: %s", offendingSpecification));

        this.offendingSpecification = offendingSpecification;
    }

    public GroupSpecification getOffendingSpecification() {
        return offendingSpecification;
    }
}
