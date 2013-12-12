package amp.tests.integration;

import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.rabbitmq.client.ConnectionFactory;

import amp.rabbit.connection.TokenConnectionFactory;
import amp.rabbit.topology.Broker;
import amp.rabbit.topology.Exchange;
import amp.rabbit.topology.ProducingRoute;

import static org.mockito.Mockito.*;

public class TokenConnectionFactoryTests {
    
	protected static FileSystemXmlApplicationContext context;
	protected static TokenConnectionFactory factory;
	
	@BeforeClass
	public static void BeforeAllTests(){
		context = new FileSystemXmlApplicationContext(Config.Authorization.AnubisBasic, Config.Topology.Simple);
		factory = (TokenConnectionFactory) context.getBean("connectionFactory");
	}
	
	@AfterClass
	public static void AfterAllTests(){
		context.close();
	}
	
    @Test
    public void Should_be_able_to_get_token_from_Anubis() throws Exception
    {
    	ConnectionFactory rabbitFactory = mock(ConnectionFactory.class);
		
    	ProducingRoute route = new ProducingRoute(
    			new ArrayList<Broker>(),
    			new Exchange(), 
    			new ArrayList<String>());
    	
    	factory.configureConnectionFactory(rabbitFactory, new Broker());
    	
    	verify(rabbitFactory).setUsername((String)isNotNull());
    	verify(rabbitFactory).setPassword((String)isNotNull());
    }
}
