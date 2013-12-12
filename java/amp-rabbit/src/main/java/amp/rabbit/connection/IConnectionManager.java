package amp.rabbit.connection;

import java.io.IOException;

import com.rabbitmq.client.Channel;

/** 
 * The connection manager's job is to manage connections more/less for 
 * these rules:
 *   1.  There should be only one connection per broker 
 *       (As many channels can then be created on that one channel)
 *   2.  To monitor the connection and restart the connection as needed.
 *   3.  To notify registered event handlers of connection changes.
 *   
 * @author jmccune
 * @author kbaltrinic
 */
public interface IConnectionManager {

	public abstract Channel createChannel() throws IOException,
			InterruptedException;

	public abstract void addConnectionEventHandler(IConnectionEventHandler handler);

	public abstract void removeConnectionEventHandler(IConnectionEventHandler handler);

	public abstract void dispose();

}