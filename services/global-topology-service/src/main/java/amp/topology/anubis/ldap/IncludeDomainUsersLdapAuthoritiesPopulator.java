package amp.topology.anubis.ldap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;

import java.util.HashSet;
import java.util.Set;

public class IncludeDomainUsersLdapAuthoritiesPopulator extends DefaultLdapAuthoritiesPopulator {

    private static final Logger Log = LoggerFactory.getLogger(IncludeDomainUsersLdapAuthoritiesPopulator.class);


    public IncludeDomainUsersLdapAuthoritiesPopulator(ContextSource contextSource, String groupSearchBase) {
        super(contextSource, groupSearchBase);
    }


    @Override
    protected Set<GrantedAuthority> getAdditionalRoles(DirContextOperations user, String username) {

        Set<GrantedAuthority> additionalAuthorities = new HashSet<GrantedAuthority>();

        // get the primaryGroupToken of Domain Users
        String primaryGroup = user.getStringAttribute("primaryGroupID");

        if (primaryGroup.equalsIgnoreCase("513")) {
            Log.info("User '{}' has primary group set to 513, which indicates a domain user. Adding that authority.", username);
            additionalAuthorities.add(new SimpleGrantedAuthority("Domain Users"));
        }

        return additionalAuthorities;
    }
}
