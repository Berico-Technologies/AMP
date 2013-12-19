package amp.topology.anubis;

import java.util.Collection;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public interface Member {

    Collection<Group> getMemberships();

    // A flattened list of all memberships, including those inherited by Groups this member belongs to.
    Collection<String> getMembershipIds();
}
