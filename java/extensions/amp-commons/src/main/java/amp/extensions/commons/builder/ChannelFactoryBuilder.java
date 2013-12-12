package amp.extensions.commons.builder;

import amp.rabbit.BasicChannelFactory;
import amp.rabbit.CertificateChannelFactory;

/**
 * Build up a Channel Factory.
 * 
 * @author Richard Clayton (Berico Technologies)
 */
public class ChannelFactoryBuilder extends FluentExtension {
	
	TransportBuilder transportBuilder;
	
	/**
	 * Initialize the builder with a reference to the parent fluent interface.
	 * @param parent Parent builder.
	 */
	public ChannelFactoryBuilder(BusBuilder parent, TransportBuilder transportBuilder) {
		
		super(parent);
		
		this.transportBuilder = transportBuilder;
	}

	/**
	 * Connect to brokers using basic authentication.
	 * @param username Username.
	 * @param password Password.
	 * @return Parent fluent.
	 */
	public TransportBuilder basicAuth(String username, String password){
		
		BasicChannelFactory channelFactory = new BasicChannelFactory(username, password);
		
		this.transportBuilder.setChannelFactory(channelFactory);
		
		return this.transportBuilder;
	}
	
	/**
	 * Connect to brokers using SSL (certificate authentication).
	 * @param keystore Path to Client Certificate.
	 * @param certPassword Password for Certificate.
	 * @param pathToTrustStore Path to Trust Store.
	 * @return Parent fluent.
	 */
	public TransportBuilder sslAuth(
		String pathToClientCert, String certPassword, String pathToTrustStore){
		
		CertificateChannelFactory channelFactory =
			new CertificateChannelFactory(pathToClientCert, certPassword, pathToTrustStore);
		
		this.transportBuilder.setChannelFactory(channelFactory);
		
		return this.transportBuilder;
	}

	/**
	 * And go back to the primary fluent interface.
	 * @return original interface.
	 */
	@Override
	public BusBuilder and() {
		
		return this.transportBuilder.and();
	}
	
	
}
