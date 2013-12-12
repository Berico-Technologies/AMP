package amp.rabbit;


import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import amp.rabbit.topology.Exchange;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultSaslConfig;


public class CertificateChannelFactory extends BaseChannelFactory {


    protected Logger log;
	protected String keystorePassword;
    protected String keystore;
    protected String truststore;


    public CertificateChannelFactory(String keystore, String keystorePassword, String truststore) {

        log = LoggerFactory.getLogger(this.getClass());
        this.keystore = keystore;
        this.keystorePassword = keystorePassword;
        this.truststore = truststore;
    }

	
	@Override
	public void configureConnectionFactory(ConnectionFactory factory, Exchange exchange) throws Exception {

        log.debug("Getting connection for exchange: {}", exchange.toString());


        char[] charPassword = (keystorePassword == null)? null : keystorePassword.toCharArray();

        KeyStore loadedKeystore = null;
        KeyStore loadedTruststore = null;

        try {


            loadedKeystore = CertificateChannelFactory.loadKeystore(
                    this.openResource(this.keystore),
                    this.keystorePassword);

            if (this.truststore != null) {

                loadedTruststore = CertificateChannelFactory.loadKeystore(
                        this.openResource(this.truststore),
                        null);
            }

        } catch (Exception e){

            log.error("Failed to configure the given ConnectionFactory: {}", e);
        }


        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(loadedKeystore, charPassword);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(loadedTruststore);

        SSLContext ctx = SSLContext.getInstance("TLSv1");
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

    	super.configureConnectionFactory(factory, exchange);
        factory.setSaslConfig(DefaultSaslConfig.EXTERNAL);
        factory.useSslProtocol(ctx);
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