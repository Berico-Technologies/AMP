package amp.anubis.services;

import amp.anubis.core.AnubisException;
import amp.anubis.core.ITokenManager;
import amp.anubis.core.NamedToken;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class DefaultTokenManager implements ITokenManager {

    private static final Logger Log = LoggerFactory.getLogger(DefaultTokenManager.class);

    private static final int DEFAULT_PASSWORD_SIZE = 16;
    private static final int DEFAULT_CACHE_ACCESS_TIMEOUT_DURATION = 10;
    private static final TimeUnit DEFAULT_CACHE_ACCESS_TIMEOUT_UNITS = TimeUnit.MINUTES;
    private static final int DEFAULT_CACHE_ABSOLUTE_EXPIRATION_DURATION = 8;
    private static final TimeUnit DEFAULT_CACHE_ABSOLUTE_EXPIRATION_UNITS = TimeUnit.HOURS;

    private Cache<String, String> _tokenCache;
    private int _cacheAccessTimeoutDuration;
    private TimeUnit _cacheAccessTimeoutUnits;
    private int _cacheAbsoluteExpirationDuration;
    private TimeUnit _cacheAbsoluteExpirationUnits;


    public void setCacheAccessTimeoutDuration(int value) { _cacheAccessTimeoutDuration = value; }
    public void setCacheAccessTimeoutUnits(TimeUnit value) { _cacheAccessTimeoutUnits = value; }
    public void setCacheAbsoluteExpirationDuration(int value) { _cacheAbsoluteExpirationDuration = value; }
    public void setDefaultCacheAbsoluteExpirationUnits(TimeUnit value) { _cacheAbsoluteExpirationUnits = value; }


    public DefaultTokenManager() {

        _cacheAccessTimeoutDuration = DEFAULT_CACHE_ACCESS_TIMEOUT_DURATION;
        _cacheAccessTimeoutUnits = DEFAULT_CACHE_ACCESS_TIMEOUT_UNITS;
        _cacheAbsoluteExpirationDuration = DEFAULT_CACHE_ABSOLUTE_EXPIRATION_DURATION;
        _cacheAbsoluteExpirationUnits = DEFAULT_CACHE_ABSOLUTE_EXPIRATION_UNITS;

        _tokenCache = CacheBuilder.newBuilder()
                .expireAfterAccess(_cacheAccessTimeoutDuration, _cacheAccessTimeoutUnits)
                .expireAfterWrite(_cacheAbsoluteExpirationDuration, _cacheAbsoluteExpirationUnits)
                .removalListener(new RemovalListener<String, String>() {
                    @Override
                    public void onRemoval(RemovalNotification<String, String> removalNotification) {
                        String snippedToken = snipToken(removalNotification.getKey(), 4);
                        Log.info("Token ending in {} removed for user {}.", snippedToken, removalNotification.getValue());
                    }
                })
                .build();
    }


    @Override
    public NamedToken generateToken(UserDetails requestor) throws AnubisException {

        // make sure we have a requestor identity
        if ( (null == requestor) || (null == requestor.getUsername()) || (requestor.getUsername().length() == 0) )
            throw new AnubisException("Username must be provided in order to generate a token.");


        // the actor for whom we're generating a token
        String identity = requestor.getUsername();
        Log.debug("Now generating a token for {}", identity);

        // generate a 128-bit (16 byte) UUID
        UUID password = UUID.randomUUID();

        // get the bytes from that UUID
        byte[] passwordBytes = this.getPasswordBytes(password);

        Log.debug("Token successfully generated.  Encoding into Base64.");
        // use the identity and password to create a named token
        NamedToken token = new NamedToken(identity, Base64.encodeBase64String(passwordBytes));
        Log.debug("Token successfully Base64 encoded");

        // add the token to the user's list of tokens
        this.cacheToken(token);
        Log.debug("Token ending {} added to authentication cache for {}.", snipToken(token.getToken(), 4), identity);

        return token;
    }

    @Override
    public boolean verifyToken(NamedToken token) throws AnubisException {

        // if there's no token, we can't verify anything
        if (this.isNullOrEmpty(token))
            return false;

        // get the details of the token
        String identity = token.getIdentity();
        String password = token.getToken();
        Log.debug("Verifying a token ending in {} from {}", this.snipToken(password, 4), identity);

        boolean verified = false;

        String username = _tokenCache.getIfPresent(password);

        if ( (null != username) && (username.equalsIgnoreCase(identity)) ) {
            verified = true;
        }

        Log.debug("Was token ending {} valid for user {}?: {}", this.snipToken(password, 4), identity, verified);
        return verified;
    }


    public void cacheToken(NamedToken token) {

        // it might be a good idea to check that the password already exists, but
        // I know that I check for that in generateToken, so until someone extends
        // this code or uses it elsewhere, it will never already exist.
        _tokenCache.put(token.getToken(), token.getIdentity());
    }

    public boolean isNullOrEmpty(NamedToken token) {

        boolean nullOrEmpty = true;

        if (null != token) {
            String identity = token.getIdentity();
            String password = token.getToken();

            if ( (null != identity) && (identity.length() > 0) ) {

                if ( (null != password) && (password.length() > 0) ) {
                    nullOrEmpty = false;
                }
            }
        }

        return nullOrEmpty;
    }


    public final String snipToken(String token, int charsToShow) {

        String snippedToken = "";

        if (token.length() > charsToShow)
            snippedToken = token.substring(token.length() - charsToShow);

        return snippedToken;
    }


    public byte[] getPasswordBytes(UUID password) throws AnubisException {

        // see: http://stackoverflow.com/questions/6881659/how-to-convert-two-longs-to-a-byte-array-how-to-convert-uuid-to-byte-array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        try {
            dos.writeLong(password.getMostSignificantBits());
            dos.writeLong(password.getLeastSignificantBits());
            dos.flush(); // May not be necessary
        }
        catch (IOException ioex) {
            throw new AnubisException("Failed to turn the generated password into a byte array.", ioex);
        }

        return baos.toByteArray();
    }
}
