package amp.rabbit.connection;

import java.util.Collection;

import cmf.bus.IDisposable;

import amp.rabbit.topology.BaseRoute;

public interface IConnectionManagerCache extends IDisposable {
	Collection<IConnectionManager> getConnectionManagersFor(BaseRoute route) throws Exception;
}