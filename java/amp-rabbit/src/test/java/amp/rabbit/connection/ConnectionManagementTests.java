package amp.rabbit.connection;

import static com.jayway.awaitility.Awaitility.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import amp.messaging.MessageRegistration;
import amp.messaging.NullHandler;
import amp.messaging.NullMessageProcessor;
import amp.rabbit.topology.Broker;
import amp.rabbit.topology.Exchange;
import amp.rabbit.topology.SimpleTopologyService;
import amp.rabbit.transport.RabbitTransportProvider;
import amp.rabbit.transport.SimpleRoutingInfoCache;
import cmf.bus.Envelope;
import amp.messaging.EnvelopeHeaderConstants;
import cmf.bus.IEnvelopeFilterPredicate;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

public class ConnectionManagementTests {
	
    private List<Connection> _connections;
    private List<Channel> _channels;
    private Map<Connection, List<ShutdownListener>> _connectionsListeners;
    private Map<Channel, List<ShutdownListener>> _channelListeners;
    private ConnectionFactory _rmqFactory;
    
    private RabbitTransportProvider _transport;

    @Before
	public void setUp() throws Exception {

        _connections = new ArrayList<Connection>();
        _connectionsListeners = new HashMap<Connection, List<ShutdownListener>>();
        _channels = new ArrayList<Channel>();
        _channelListeners = new HashMap<Channel, List<ShutdownListener>>();
        _rmqFactory = mock(ConnectionFactory.class);
        
        when(_rmqFactory.newConnection()).thenAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				
	           Connection connection = mock(Connection.class);
	            _connections.add(connection);
	            _connectionsListeners.put(connection, new ArrayList<ShutdownListener>());

                doAnswer( new Answer(){

					@Override
					public Object answer(InvocationOnMock invocation)
							throws Throwable {
						_connectionsListeners.get((Connection)invocation.getMock())
							.add((ShutdownListener)invocation.getArguments()[0]);
						return null;
					}}).when(connection).addShutdownListener(org.mockito.Mockito.any(ShutdownListener.class));

                when(connection.createChannel()).thenAnswer(new Answer() {
					@Override
					public Object answer(InvocationOnMock invocation) throws Throwable {
						
		                Channel channel = mock(Channel.class);
		                _channels.add(channel);
		                _channelListeners.put(channel, new ArrayList<ShutdownListener>());
		                
		                doAnswer( new Answer(){

							@Override
							public Object answer(InvocationOnMock invocation)
									throws Throwable {
								_channelListeners.get((Channel)invocation.getMock())
									.add((ShutdownListener)invocation.getArguments()[0]);
								return null;
							}}).when(channel).addShutdownListener(org.mockito.Mockito.any(ShutdownListener.class));
		                
		                return channel;
		             }
	            });
                
                //We are assuming that the purpose of calling isOpen is see if it is safe to close the connection.
                when(connection.isOpen()).thenReturn(true).thenReturn(false);

		        return connection;
	               
            }
        });

        _transport = new RabbitTransportProvider(
            new SimpleTopologyService(null, new Broker("nowhere.com", 0)),
            new TestConnectionFactory(_rmqFactory),
            new SimpleRoutingInfoCache(100));
   	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
    public void Send_should_create_a_connection_and_a_channel() throws Exception {
		
        sendMessage();

        verify(_rmqFactory).newConnection();
        verify(_connections.get(0)).createChannel();
    }

	@Test
    public void Register_should_create_a_connection_and_a_channel() throws Exception {
		registerNullHandler();

        verify(_rmqFactory, timeout(250)).newConnection();
        verify(_connections.get(0)).createChannel();
    }
	
	@Test
    public void Register_should_restart_on_new_channel_if_channel_fails() throws Exception
    {
		registerNullHandler();

        simulateChannelClosure(false);

        verify(_rmqFactory, times(1)).newConnection();
        verify(_connections.get(0), timeout(250).times(2)).createChannel();
     }

	@Test
    public void Register_should_not_restart_if_channel_is_closed_by_application() throws Exception
    {
		registerNullHandler();

        simulateChannelClosure(true);

        Thread.sleep(250); //wait long enough that if it was going to restart it would have.
        
        verify(_rmqFactory, times(1)).newConnection();
        verify(_connections.get(0), times(1)).createChannel();
    }

	@Test
    public void Register_should_restart_on_new_connection_if_connection_fails() throws Exception {
		registerNullHandler();

		simulateConnectionClosure(false);

        verify(_rmqFactory, timeout(250).times(2)).newConnection();
        verify(_connections.get(0), times(1)).createChannel();
        verify(_connections.get(1), timeout(100).times(1)).createChannel();
    }

	@Test
    public void Register_should_not_restart_if_connection_is_closed_by_application() throws Exception {
		registerNullHandler();

        simulateConnectionClosure(true);

        Thread.sleep(250); //wait long enough that if it was going to restart it would have.

        verify(_rmqFactory, times(1)).newConnection();
    }

	@Test
    public void Closing_the_transport_should_close_all_connections() throws Exception {
        
		sendMessage();
        registerNullHandler();

        _transport.dispose();

        for(Connection connection : _connections) {
            verify(connection).close();
        }
    }

	private void sendMessage() throws Exception {
		Envelope env = new Envelope();
        env.setHeader(EnvelopeHeaderConstants.MESSAGE_TOPIC, "testing");

        _transport.send(env);
	}
    
    private void registerNullHandler() throws Exception {
		Envelope env = new Envelope();
        env.setHeader(EnvelopeHeaderConstants.MESSAGE_TOPIC, "testing");

        _transport.register(new MessageRegistration(new NullMessageProcessor(), new NullHandler(),new IEnvelopeFilterPredicate() {
			
			@Override
			public boolean filter(Envelope envelope) {
				return true;
			}
		}));
	}

    private void simulateChannelClosure(boolean initiatedByApplication) throws Exception{
    	
    	waitAtMost(250, TimeUnit.MILLISECONDS).untilCall(to(_channels).size(), is(1));
    	
        ShutdownSignalException ex = new ShutdownSignalException(false, initiatedByApplication, null, null);

    	for(ShutdownListener listener : _channelListeners.get(_channels.get(0))){
    		listener.shutdownCompleted(ex);
    	}
    }

    private void simulateConnectionClosure(boolean initiatedByApplication) throws Exception {
    	
    	waitAtMost(250, TimeUnit.MILLISECONDS).untilCall(to(_channels).size(), is(1));

        ShutdownSignalException ex = new ShutdownSignalException(false, initiatedByApplication, null, null);

    	for(ShutdownListener listener : _connectionsListeners.get(_connections.get(0))){
    		listener.shutdownCompleted(ex);
    	}
    }
    
    private static class TestConnectionFactory extends BaseConnectionFactory {
    	
        private final ConnectionFactory _factory;
        
        public TestConnectionFactory(ConnectionFactory factory) {
            _factory = factory;
        }

        @Override
        public IConnectionManager getConnectionManagerFor(Broker broker) throws Exception {
            return new ConnectionManager(_factory);    
        }
    }
}
