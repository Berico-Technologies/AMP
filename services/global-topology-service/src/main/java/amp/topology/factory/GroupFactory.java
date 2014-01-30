package amp.topology.factory;

import amp.topology.global.Partition;

/**
 * Encapsulates the construction of groups, hiding as much detail as possible from the requester.
 *
 * The GroupFactory not only creates the group, but also registers the group with the correct BasicTopic.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public interface GroupFactory {

    /**
     * Create a BaseGroup with the provided specification.
     *
     * You are returned a copy of the BaseGroup so you can readily interact with it.  The BaseGroup
     * will automatically be registered with the TopicConfiguration (you don't need to do anything else).
     *
     * @param specification Specification of the BaseGroup.
     * @return GroupSpecification_3_3_0
     * @throws Exception an error occurring during the construction or registration process.
     */
    amp.topology.global.Group<? extends Partition> create(GroupSpecification specification) throws Exception;

    /**
     * Modify the provided group with the state represented by the specification.
     *
     * @param specification Specification of the BaseGroup (representing the mutations).
     * @return a collection of modification entries describing the results of the operation.
     * @throws Exception Any error that occurred.
     */
    Modifications modify(GroupSpecification specification) throws Exception;
}
