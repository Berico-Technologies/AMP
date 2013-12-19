package amp.topology.anubis;

import java.util.Collection;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public interface Group extends Member {

    String getId();

    Collection<Member> getMembers();
}
