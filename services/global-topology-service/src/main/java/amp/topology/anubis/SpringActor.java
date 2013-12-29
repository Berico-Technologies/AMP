package amp.topology.anubis;

import com.google.common.collect.Lists;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class SpringActor implements Actor {

    private UserDetails userDetails;

    public SpringActor(UserDetails userDetails) {
        this.userDetails = userDetails;
    }

    @Override
    public String getId() {
        return userDetails.getUsername();
    }

    @Override
    public Collection<Group> getMemberships() {

        Collection<String> authorities = getAuthorities();

        Collection<Group> groups = Lists.newArrayList();

        for (String authority : authorities){

            groups.add(new FlatGroup(authority, this));
        }

        return groups;
    }

    @Override
    public Collection<String> getMembershipIds() {

        return getAuthorities();
    }

    private Collection<String> getAuthorities(){

        ArrayList<String> authorities = Lists.newArrayList();

        for (GrantedAuthority authority : userDetails.getAuthorities()){

            authorities.add(authority.getAuthority());
        }

        return authorities;
    }
}
