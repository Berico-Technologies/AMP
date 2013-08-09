package amp.utility.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
		
			KeyStore loadedKeystore = getAndLoad(this.keystore, this.keystorePassword);
			
			if (this.truststore != null){
				
				KeyStore loadedTruststore = getAndLoad(this.truststore, this.truststorePassword);
				
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

	/**
	 * Get an instance of the KeyStore and Load it with Certs using the supplied password.
	 * @param path Location of the KeyStore
	 * @param password Password, which can be null for the trust store.
	 * @return A loaded KeyStore instance.
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException 
	 * @throws CertificateException
	 * @throws IOException
	 */
	static KeyStore getAndLoad(String path, String password) 
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		
		char[] charPassword = (password == null)? null : password.toCharArray();
		
		KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
		
		FileInputStream fis = new FileInputStream(new File(path));
		
		keystore.load(fis, charPassword);
		
		return keystore;
	}
}
