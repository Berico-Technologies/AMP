package amp.anubis.tests;

import amp.anubis.resources.RabbitAuthorizationResource;
import amp.anubis.core.IRabbitAccessManager;
import org.junit.Test;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class RabbitAuthorizationResourceTests {

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_IllegalArgumentException_if_userDetailsService_is_null() {

        // create
        IRabbitAccessManager mockAccessManager = mock(IRabbitAccessManager.class);

        // test
        RabbitAuthorizationResource authResource = new RabbitAuthorizationResource(null, mockAccessManager);

        // assert
        fail("Should have thrown IllegalArgumentException but didn't.");
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_IllegalArgumentException_if_accessManager_is_null() {

        // create
        UserDetailsService mockUserDetailsService = mock(UserDetailsService.class);

        // test
        RabbitAuthorizationResource authResource = new RabbitAuthorizationResource(mockUserDetailsService, null);

        // assert
        fail("Should have thrown IllegalArgumentException but didn't.");
    }

    @Test
    public void should_return_AccessLevel_NONE_if_UserDetailsService_throws_UsernameNotFoundException() {

        // create
        UserDetailsService mockUserDetailsService = mock(UserDetailsService.class);
        IRabbitAccessManager mockAccessManager = mock(IRabbitAccessManager.class);
        RabbitAuthorizationResource rabbitResource = new RabbitAuthorizationResource(mockUserDetailsService, mockAccessManager);

        // setup
        when(mockUserDetailsService.loadUserByUsername("")).thenThrow(new UsernameNotFoundException("test exception"));

        // test
        String response = rabbitResource.authorizeUser("", "");

        // assert
        assertEquals("deny", response);
    }
}
