package amp.rabbit;

import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import amp.rabbit.topology.Exchange;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultSaslConfig;

public class SSLChannelFactory extends BaseChannelFactory {

    private String _pathToRabbitTrustStore;
    private String _keystorePassword;
    private String _username;
    private String _password;


    public void setUsername(String value) { _username = value; }
    public void setPassword(String value) { _password = value; }


    public SSLChannelFactory(String pathToRabbitTrustStore, String keystorePassword) {
        _pathToRabbitTrustStore = pathToRabbitTrustStore;
        _keystorePassword = keystorePassword;
    }

    public SSLChannelFactory(
            String username,
            String password,
            String pathToRabbitTrustStore,
            String keystorePassword) {

        _username = username;
        _password = password;
        _pathToRabbitTrustStore = pathToRabbitTrustStore;
        _keystorePassword = keystorePassword;
    }


    @Override
	public void configureConnectionFactory(ConnectionFactory factory, Exchange exchange) throws Exception {
    	super.configureConnectionFactory(factory, exchange);
    	
        // apparently, a string isn't good enough
        char[] keyPassphrase = _keystorePassword.toCharArray();

        // load the java key store
        KeyStore remoteCertStore = KeyStore.getInstance("JKS");
        remoteCertStore.load(new FileInputStream(_pathToRabbitTrustStore), keyPassphrase);

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