package amp.topology.anubis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class FlatGroup implements Group {

    private String authority;
    private Member[] members;

    public FlatGroup(String authority, Member... members) {
        this.authority = authority;
        this.members = members;
    }

    @Override
    public String getId() {
        return authority;
    }

    @Override
    public Collection<Member> getMembers() {
        return Arrays.asList(members);
    }

    @Override
    public Collection<Group> getMemberships() {

        return new ArrayList<Group>();
    }

    @Override
    public Collection<String> getMembershipIds() {

        return new ArrayList<String>();
    }
}
