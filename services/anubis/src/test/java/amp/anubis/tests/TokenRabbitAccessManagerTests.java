package amp.anubis.tests;

import amp.anubis.core.AttributedNamedToken;
import amp.anubis.core.IRabbitAccessManager;
import amp.anubis.core.ITokenManager;
import amp.anubis.services.TokenRabbitAccessManager;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TokenRabbitAccessManagerTests {

    @Test(expected = IllegalArgumentException.class)
    public void should_throw_IllegalArgumentException_if_tokenManager_is_null() {

        TokenRabbitAccessManager accessManager = new TokenRabbitAccessManager(null);

        fail("should have thrown an IllegalArgumentException but didn't.");
    }

    @Test
    public void should_return_AccessLevel_NONE_if_AttributedNamedToken_is_null() {

        // create
        ITokenManager tokenManager = mock(ITokenManager.class);
        TokenRabbitAccessManager accessManager = new TokenRabbitAccessManager(tokenManager);
        //NamedToken fakeToken = new NamedToken("", "");

        // test
        IRabbitAccessManager.AccessLevel access = accessManager.authorizeUser(null);

        // assert
        assertEquals(IRabbitAccessManager.AccessLevel.NONE, access);
    }

    @Test
    public void should_return_AccessLevel_NONE_if_identity_is_null() {

        // create
        AttributedNamedToken attributedNamedToken = new AttributedNamedToken(null, "", null);
        ITokenManager tokenManager = mock(ITokenManager.class);
        TokenRabbitAccessManager accessManager = new TokenRabbitAccessManager(tokenManager);

        // test
        IRabbitAccessManager.AccessLevel access = accessManager.authorizeUser(attributedNamedToken);

        // assert
        assertEquals(IRabbitAccessManager.AccessLevel.NONE, access);
    }

    @Test
    public void should_return_AccessLevel_NONE_if_identity_is_empty() {

        // create
        AttributedNamedToken attributedNamedToken = new AttributedNamedToken("", "", null);
        ITokenManager tokenManager = mock(ITokenManager.class);
        TokenRabbitAccessManager accessManager = new TokenRabbitAccessManager(tokenManager);

        // test
        IRabbitAccessManager.AccessLevel access = accessManager.authorizeUser(attributedNamedToken);

        // assert
        assertEquals(IRabbitAccessManager.AccessLevel.NONE, access);
    }

    @Test
    public void should_return_AccessLevel_NONE_if_password_is_null() {

        // create
        AttributedNamedToken attributedNamedToken = new AttributedNamedToken("testUser", null, null);
        ITokenManager tokenManager = mock(ITokenManager.class);
        TokenRabbitAccessManager accessManager = new TokenRabbitAccessManager(tokenManager);

        // test
        IRabbitAccessManager.AccessLevel access = accessManager.authorizeUser(attributedNamedToken);

        // assert
        assertEquals(IRabbitAccessManager.AccessLevel.NONE, access);
    }

    @Test
    public void should_return_AccessLevel_NONE_if_password_is_empty() {

        // create
        AttributedNamedToken attributedNamedToken = new AttributedNamedToken("testUser", "", null);
        ITokenManager tokenManager = mock(ITokenManager.class);
        TokenRabbitAccessManager accessManager = new TokenRabbitAccessManager(tokenManager);

        // test
        IRabbitAccessManager.AccessLevel access = accessManager.authorizeUser(attributedNamedToken);

        // assert
        assertEquals(IRabbitAccessManager.AccessLevel.NONE, access);
    }
}
