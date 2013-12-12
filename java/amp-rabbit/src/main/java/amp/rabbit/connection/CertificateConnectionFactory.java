package amp.rabbit.connection;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import amp.rabbit.topology.Broker;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultSaslConfig;


public class CertificateConnectionFactory extends BaseConnectionFactory {


    protected Logger log;
	protected String keystorePassword;
    protected String keystore;
    protected String truststore;

    public CertificateConnectionFactory(String keystore, String keystorePassword, String truststore) {

        log = LoggerFactory.getLogger(this.getClass());
        this.keystore = keystore;
        this.keystorePassword = keystorePassword;
        this.truststore = truststore;
    }

	
	@Override
	public void configureConnectionFactory(ConnectionFactory factory, Broker broker) throws Exception {

        log.debug("Getting connection for broker: {}", broker);


        char[] charPassword = (keystorePassword == null)? null : keystorePassword.toCharArray();

        KeyStore loadedKeystore = null;
        KeyStore loadedTruststore = null;

        try {

            loadedKeystore = getAndLoad(this.keystore, this.keystorePassword);

            if (this.truststore != null) {

                loadedTruststore = getAndLoad(this.truststore, null);
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

    	super.configureConnectionFactory(factory, broker);
        factory.setSaslConfig(DefaultSaslConfig.EXTERNAL);
        factory.useSslProtocol(ctx);
	}


    /**
     * Get an instance of the KeyStore and Load it with Certs using the supplied password.
     * @param path Location of the KeyStore
     * @param password Password, which can be null for the trust store.
     * @return A loaded KeyStore instance.
     * @throws java.security.KeyStoreException
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.security.cert.CertificateException
     * @throws java.io.IOException
     */
    static KeyStore getAndLoad(String path, String password)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, URISyntaxException {

        char[] charPassword = (password == null)? null : password.toCharArray();

        KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());

        File keystoreFile = new File(path);

        if (!keystoreFile.exists()) {
            // try to find it on the classpath
            URL url = CertificateConnectionFactory.class.getResource(path);
            keystoreFile = (null != url) ? new File(url.toURI()) : new File(path);
        }

        FileInputStream fis = new FileInputStream(keystoreFile);

        keystore.load(fis, charPassword);

        return keystore;
    }
}
