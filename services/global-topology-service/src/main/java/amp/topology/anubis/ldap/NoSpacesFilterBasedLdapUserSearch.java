package amp.topology.anubis.ldap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;

public class NoSpacesFilterBasedLdapUserSearch extends FilterBasedLdapUserSearch {

    private static final Logger Log = LoggerFactory.getLogger(NoSpacesFilterBasedLdapUserSearch.class);


    public NoSpacesFilterBasedLdapUserSearch(String searchBase, String searchFilter, BaseLdapPathContextSource contextSource) {
        super(searchBase, searchFilter, contextSource);
    }


    @Override
    public DirContextOperations searchForUser(String username) {

        // builder is not synchronized, which we don't need
        StringBuilder buffer = new StringBuilder();

        String[] tokens = username.split(",");
        for (String token : tokens) {
            buffer.append(token.trim());
            buffer.append(",");
        }


        // due to the way I loop, there's an extra comma which we need to cleanup
        // but only if we had tokens (not a garauntee),
        if (tokens.length > 0) {
            buffer.deleteCharAt(buffer.length() - 1);
        }

        // get the new username
        String newUsername = buffer.toString();

        Log.info("Changed username {} to {}", username, newUsername);

        // return the result of the parent class with the new de-spaced string
        return super.searchForUser(newUsername);
    }
}
