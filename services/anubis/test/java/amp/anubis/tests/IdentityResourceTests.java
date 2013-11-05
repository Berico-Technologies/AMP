package amp.anubis.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import amp.anubis.core.AnubisException;
import amp.anubis.core.ITokenManager;
import amp.anubis.core.NamedToken;
import amp.anubis.resources.IdentityResource;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.springframework.security.core.userdetails.UserDetails;

@RunWith(Enclosed.class)
public class IdentityResourceTests {

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_IllegalArgumentException_if_tokenManager_isNull() {
        IdentityResource idResource = new IdentityResource(null);

        fail("An IllegalArgumentException should have been thrown, but it wasn't.");
    }


    public static class GenerateTokenTests {

        private IdentityResource _idResource;
        private ITokenManager _tokenManager;


        @Before
        public void beforeEachTest() {
            _tokenManager = mock(ITokenManager.class);
            _idResource = new IdentityResource(_tokenManager);
        }

        @Test
        public void should_return_JSONPObject_if_callback_present() throws AnubisException {

            // mocks & fakes
            NamedToken fakeToken = new NamedToken("testUser", "TOKEN");
            UserDetails mockUser = mock(UserDetails.class);

            // setup
            when(_tokenManager.generateToken(mockUser)).thenReturn(fakeToken);

            // test
            Object result = _idResource.getNamedToken(mockUser, "callback");

            // assert
            assertTrue(result instanceof JSONPObject);
        }

        @Test
        public void should_return_NamedToken_if_callback_absent() throws AnubisException {

            // fakes and mocks
            NamedToken fakeToken = new NamedToken("testUser", "TOKEN");
            UserDetails mockUser = mock(UserDetails.class);

            // setup
            when(_tokenManager.generateToken(mockUser)).thenReturn(fakeToken);

            // test
            Object result = _idResource.getNamedToken(mockUser, null);

            // assert
            Class<NamedToken> namedTokenClass = NamedToken.class;
            assertTrue(namedTokenClass.isInstance(result));
        }

        @Test
        public void should_return_NamedToken_if_callback_empty() throws AnubisException {

            // fakes and mocks
            NamedToken fakeToken = new NamedToken("testUser", "TOKEN");
            UserDetails mockUser = mock(UserDetails.class);

            // setup
            when(_tokenManager.generateToken(mockUser)).thenReturn(fakeToken);

            // test
            Object result = _idResource.getNamedToken(mockUser, "");

            // assert
            Class<NamedToken> namedTokenClass = NamedToken.class;
            assertTrue(namedTokenClass.isInstance(result));
        }

        @Test(expected = AnubisException.class)
        public void should_throw_AnubisException_if_tokenManager_returns_null() throws AnubisException {

            // fakes and mocks
            UserDetails mockUser = mock(UserDetails.class);

            // setup
            when(_tokenManager.generateToken(mockUser)).thenReturn(null);

            // test
            _idResource.getNamedToken(mockUser, null);

            // assert
            fail("An AnubisException should have been thrown and wasn't.");
        }
    }
}
