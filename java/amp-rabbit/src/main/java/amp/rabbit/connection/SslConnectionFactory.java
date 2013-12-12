package amp.rabbit.connection;

import java.security.KeyStore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import amp.rabbit.topology.Broker;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultSaslConfig;

public class SslConnectionFactory extends BaseConnectionFactory {

    private String _truststore;
    private String _username;
    private String _password;


    public void setUsername(String value) { _username = value; }
    public void setPassword(String value) { _password = value; }


    public SslConnectionFactory(String truststore) {
        _truststore = truststore;
    }

    public SslConnectionFactory(
            String username,
            String password,
            String truststore) {

        _username = username;
        _password = password;
        _truststore = truststore;
    }


    @Override
	public void configureConnectionFactory(ConnectionFactory factory, Broker broker) throws Exception {
    	super.configureConnectionFactory(factory, broker);
    	
        // load the java key store
        KeyStore remoteCertStore = CertificateConnectionFactory.getAndLoad(_truststore, null); //KeyStore.getInstance("JKS");
        
        // use it to build the trust manager
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(remoteCertStore);

        // initialize a context for SSL-protected (not mutual auth) connection
        SSLContext c = SSLContext.getInstance("TLSv1");
        c.init(null, tmf.getTrustManagers(), null);

        factory.setUsername(_username);
        factory.setPassword(_password);
        factory.setSaslConfig(DefaultSaslConfig.EXTERNAL);
        factory.useSslProtocol(c);
    }
}