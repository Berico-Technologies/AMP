package amp.topology.global.persistence.mapdb;

import amp.topology.global.exceptions.ConnectorNotExistException;
import amp.topology.global.impl.BaseConnector;
import amp.topology.global.persistence.TopologyStatePersister;
import org.mapdb.DB;

import java.util.Iterator;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class MapDbConnectorPersister
        extends MapDbPersister<BaseConnector.DehydratedState>
        implements TopologyStatePersister.ConnectorStatePersister {

    public MapDbConnectorPersister(DB configuredDatabase) {

        super(configuredDatabase);
    }

    @Override
    public String getCollectionName() {

        return "connectors";
    }

    @Override
    public void save(BaseConnector.DehydratedState state) {

        map().put(state.getConnectorId(), state);
    }

    @Override
    public BaseConnector.DehydratedState get(String id) throws ConnectorNotExistException {

        BaseConnector.DehydratedState state = map().get(id);

        if (state == null) throw new ConnectorNotExistException("unknown", id);

        return state;
    }

    @Override
    public boolean exists(String id) {

        return map().containsKey(id);
    }

    @Override
    public void remove(String id) throws ConnectorNotExistException {

        BaseConnector.DehydratedState state = map().remove(id);

        if (state == null) throw new ConnectorNotExistException("unknown", id);
    }

    @Override
    public Iterator<String> recordIdIterator() {

        return map().keySet().iterator();
    }
}
