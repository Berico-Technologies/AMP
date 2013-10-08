package amp.rabbit.connection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;

import cmf.bus.IDisposable;

public class ConnectionManager implements IDisposable {

	private static final Logger LOG = LoggerFactory.getLogger(ConnectionManager.class);
	
    private ConnectionFactory _factory;
    private Connection _connection;
    private Semaphore _connectionAccessSemaphor;
    private List<IConnectionEventHandler> _eventHandlers = new ArrayList<IConnectionEventHandler>();
    
    public ConnectionManager(ConnectionFactory factory) throws IOException {
        _factory = factory;
        _connection = _factory.newConnection();
        _connection.addShutdownListener(new ConnectionShutdownHandler());
        _connectionAccessSemaphor = new Semaphore(1);
        }

    public Channel createChannel() throws IOException, InterruptedException{

    	//Ensure we are not in the midst of a reconnect attempt. If so wait till we reconnect.
        _connectionAccessSemaphor.acquire();
    	try{
	       return _connection.createChannel();
      	}  
    	finally {
    		_connectionAccessSemaphor.release();
    	}
    }
    
    public void addConnectionEventHandler(IConnectionEventHandler handler){
    	_eventHandlers.add(handler);
    }
    
    public void removeConnectionEventHandler(IConnectionEventHandler handler){
    	_eventHandlers.remove(handler);
    }

    @Override
	public void dispose() {
		if(_connection != null){
			try {
				_connection.close();
			} catch (IOException e) {
				LOG.warn("Error while closing connection.", e);
			}
		}
	}
    
    private class ConnectionShutdownHandler implements ShutdownListener {

		@Override
		public void shutdownCompleted(ShutdownSignalException cause) {
			
			LOG.debug("Enter shutdownCompleted handler, isInitiatedByApplication: " + cause.isInitiatedByApplication());
            LOG.error("Connection Closed.", cause);

            _connection = null;

            boolean shouldAttemtToReopen = !cause.isInitiatedByApplication();
                
            for(IConnectionEventHandler handler : _eventHandlers) {
                try {
                	handler.onConnectionClosed(shouldAttemtToReopen);
                } 
                catch (Exception ex) {
                	LOG.warn("onConnectionClosed handler threw an exception.", ex);
                }
            }

            if (shouldAttemtToReopen) {
                try {
                	//Keep people from trying to use connection while we are reconnecting.
					_connectionAccessSemaphor.acquire();
				} catch (InterruptedException e) {
					LOG.error("Thread interupted attempting to aquire connectionSemapphore prior to attempting reconnect.", e);
				}
                //Do actual reconnect attempts on background thread
                Thread reconnectThread = new Thread(new ReconnectionAttempter());
                reconnectThread.setName("Connection Reconnect Thread " + this.hashCode());
                reconnectThread.start();
            }
            
            LOG.debug("Leave shutdownCompleted handler.");		
        }
    }
    
    private class ReconnectionAttempter implements Runnable {

		@Override
		public void run() {
            //TODO: Make timeout and attempt frequency configurable.
            DateTime endTime = DateTime.now().plusMinutes(5);
            while (endTime.isAfterNow())
            {
                try
                {
                    _connection = _factory.newConnection();
                    _connection.addShutdownListener(new ConnectionShutdownHandler());
                    //release access to the connection;
                    _connectionAccessSemaphor.release();
                    LOG.info("Successfully reconnected.");

                    for(IConnectionEventHandler handler : _eventHandlers) {
                        try {
                        	handler.onConnectionReconnected();
                        } 
                        catch (Exception ex) {
                        	LOG.warn("onConnectionReconnected handler threw an exception.", ex);
                        }
                    }
                    
                    return;
                }
                catch (Exception ex)
                {
                	LOG.error("Reconnect attempt failed.", ex);
                    try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						LOG.warn("Thread interupted while awaiting next reconnection attempt.  Aborting reconnection attempt.");
						return;
					}
                }
            }
            LOG.info("Failed to reconnect in the time allowed.  Will no longer attempt.");			
		}
    	
    }
}
