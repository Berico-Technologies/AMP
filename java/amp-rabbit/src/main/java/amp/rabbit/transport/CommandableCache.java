package amp.rabbit.transport;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import amp.commanding.ICommandReceiver;
import amp.messaging.MessageException;


/**
 * Created with IntelliJ IDEA.
 * User: John
 * Date: 5/11/13
 */
public class CommandableCache extends SimpleRoutingInfoCache {

    private static final Logger LOG = LoggerFactory.getLogger(CommandableCache.class);

    private ICommandReceiver commandReceiver;

    public CommandableCache(ICommandReceiver commandReceiver, long cacheExpiryInSeconds) {
		super(cacheExpiryInSeconds);
        this.commandReceiver = commandReceiver;

        try {
            // subscribe for the command to burst the routing cache.  Pass a cache
            // reference into the cache buster that handles incoming BurstRoutingCache commands
            this.commandReceiver.onCommandReceived(new RoutingCacheBuster(this.routingInfoCache, cacheLock));
        }
        catch (MessageException cex) {
            LOG.warn("Failed to subscribe for Routing Cache Bust commands - the cache cannot be remotely commanded.", cex);
        }
    }
    
	@Override
	public void dispose() {
	    this.commandReceiver.dispose();
	}
}
