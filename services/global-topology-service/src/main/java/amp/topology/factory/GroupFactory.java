package amp.topology.factory;

import amp.topology.global.Partition;
import amp.topology.global.TopologyGroup;

/**
 * Encapsulates the construction of groups, hiding as much detail as possible from the requester.
 *
 * The GroupFactory not only creates the group, but also registers the group with the correct Topic.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public interface GroupFactory {

    /**
     * Create a Group with the provided specification.
     *
     * You are returned a copy of the Group so you can readily interact with it.  The Group
     * will automatically be registered with the TopicConfiguration (you don't need to do anything else).
     *
     * @param specification Specification of the Group.
     * @return GroupSpecification
     * @throws Exception an error occurring during the construction or registration process.
     */
    TopologyGroup<? extends Partition> create(GroupSpecification specification) throws Exception;
}
