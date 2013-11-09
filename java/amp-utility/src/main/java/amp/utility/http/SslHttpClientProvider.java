package amp.utility.http;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an HttpClient configured for SSL Mutual Auth.
 * 
 * @author Richard Clayton (Berico Technologies)
 */
public class SslHttpClientProvider implements HttpClientProvider {

	private static final Logger logger = LoggerFactory.getLogger(SslHttpClientProvider.class);
	
	int port;
	
	String keystore;
	
	String keystorePassword;
	
	String truststore;
	
	/**
	 * It's okay if this is null if you are either not using a trust store,
	 * or the trust store has no password.
	 */
	String truststorePassword = null;
	
	/**
	 * Instantiate using a password for the TrustStore.
	 * @param keystore Path to Key store
	 * @param keystorePassword Password of Key store (private key)
	 * @param port Port of the remote server.
	 */
	public SslHttpClientProvider(String keystore, String keystorePassword, int port){
		
		this.keystore = keystore;
		this.keystorePassword = keystorePassword;
		this.port = port;
	}
	
	/**
	 * Instantiate using a password for the TrustStore.
	 * @param keystore Path to Key store
	 * @param keystorePassword Password of Key store (private key)
	 * @param truststore Path to Trust store
	 * @param port Port of the remote server.
	 */
	public SslHttpClientProvider(String keystore, String keystorePassword, String truststore, int port){
		
		this(keystore, keystorePassword, port);
		
		this.truststore = truststore;
	}
	
	/**
	 * Instantiate using a password for the TrustStore.
	 * @param keystore Path to Key store
	 * @param keystorePassword Password of Key store (private key)
	 * @param truststore Path to Trust store
	 * @param trustStorePassword Trust store password
	 * @param port Port of the remote server.
	 */
	public SslHttpClientProvider(String keystore, String keystorePassword, 
			String truststore, String trustStorePassword, int port){
		
		this(keystore, keystorePassword, truststore, port);
		
		this.truststorePassword = trustStorePassword;
	}
	
	@Override
	public HttpClient getClient() {
		
		return this.createClient();
	}

	/**
	 * Register a Trust Store with the HttpClient.
	 */
	public HttpClient createClient() {
		
		logger.debug("Creating HttpClient.");
		
		SSLSocketFactory socketFactory = null;
		
		try {
		
			KeyStore loadedKeystore = loadKeystore(
                    this.openResource(this.keystore),
                    this.keystorePassword);
			
			if (this.truststore != null) {
				
				KeyStore loadedTruststore = loadKeystore(
                        this.openResource(this.truststore),
                        this.truststorePassword);
				
				socketFactory = new SSLSocketFactory(loadedKeystore, this.keystorePassword, loadedTruststore);
				
			} else {
				
				socketFactory = new SSLSocketFactory(loadedKeystore, this.keystorePassword, loadedKeystore);
			}
			
		} catch (Exception e){  
			
			logger.error("Could not instantiate an SSLSocketFactory: {}", e);
		}
		
		Scheme scheme = new Scheme("https", this.port, socketFactory);
		
		DefaultHttpClient httpClient = new DefaultHttpClient();
		
		httpClient.getConnectionManager().getSchemeRegistry().register(scheme);
		
		return httpClient;
	}

    public InputStream openResource(String resourceName) throws FileNotFoundException {


        InputStream stream;

        // try to open resource as a file on the file system
        try {
            stream = new FileInputStream(resourceName);
            return stream;
        } catch (Exception ex) {} // it's not a file path


        // try as an embedded resource
        stream = this.getClass().getClassLoader().getResourceAsStream(resourceName);
        if (stream != null) {
            return stream;
        }


        // it is neither on the file system, nor embedded
        throw new FileNotFoundException("Failed to find file: " + resourceName);
    }

    /**
     * Get an instance of the KeyStore and Load it with Certs using the supplied password.
     * @param stream InputStreamReader with KeyStore opened for reading.
     * @param password Password, which can be null for the trust store.
     * @return A loaded KeyStore instance.
     * @throws java.security.KeyStoreException
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.security.cert.CertificateException
     * @throws java.io.IOException
     */
    static KeyStore loadKeystore(InputStream stream, String password)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, URISyntaxException {

        char[] charPassword = (password == null)? null : password.toCharArray();

        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
        keystore.load(stream, charPassword);

        return keystore;
    }
}
