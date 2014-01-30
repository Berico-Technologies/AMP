package amp.topology.global.persistence.mapdb;

import amp.topology.global.exceptions.TopologyGroupNotExistException;
import amp.topology.global.impl.BaseGroup;
import amp.topology.global.persistence.TopologyStatePersister;
import org.mapdb.DB;

import java.util.Iterator;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class MapDbGroupPersister
        extends MapDbPersister<BaseGroup.DehydratedState>
        implements TopologyStatePersister.GroupStatePersister {

    public MapDbGroupPersister(DB configuredDatabase) {

        super(configuredDatabase);
    }

    @Override
    public String getCollectionName() {

        return "groups";
    }

    @Override
    public void save(BaseGroup.DehydratedState state) {

        map().put(state.getGroupId(), state);
    }

    @Override
    public BaseGroup.DehydratedState get(String id) throws TopologyGroupNotExistException {

        BaseGroup.DehydratedState state = map().get(id);

        if (state == null) throw new TopologyGroupNotExistException("unknown", id, false);

        return state;
    }

    @Override
    public boolean exists(String id) {

        return map().containsKey(id);
    }

    @Override
    public void remove(String id) throws TopologyGroupNotExistException {

        BaseGroup.DehydratedState state = map().remove(id);

        if (state == null) throw new TopologyGroupNotExistException("unknown", id, false);
    }

    @Override
    public Iterator<String> recordIdIterator() {

        return map().keySet().iterator();
    }
}
