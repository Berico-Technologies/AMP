package amp.rabbit;


import amp.rabbit.topology.Exchange;

import com.rabbitmq.client.ConnectionFactory;


public class BasicChannelFactory extends BaseChannelFactory {

	protected String username;
	protected String password;
	
	public BasicChannelFactory(String username, String password) {
		
		super();

		this.username = username;
		this.password = password;
	}
	

	@Override
	public void configureConnectionFactory(ConnectionFactory factory, Exchange exchange) throws Exception {
    	super.configureConnectionFactory(factory, exchange);

        factory.setUsername(username);
        factory.setPassword(password);
	}
}
