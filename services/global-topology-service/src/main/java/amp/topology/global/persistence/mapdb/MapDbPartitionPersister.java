package amp.topology.global.persistence.mapdb;

import amp.topology.global.exceptions.PartitionNotExistException;
import amp.topology.global.impl.BasePartition;
import amp.topology.global.persistence.TopologyStatePersister;
import org.mapdb.DB;

import java.util.Iterator;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class MapDbPartitionPersister
        extends MapDbPersister<BasePartition.DehydratedState>
        implements TopologyStatePersister.PartitionStatePersister {

    public MapDbPartitionPersister(DB configuredDatabase) {
        super(configuredDatabase);
    }

    @Override
    public String getCollectionName() {

        return "partitions";
    }

    @Override
    public void save(BasePartition.DehydratedState state) {

        map().put(state.getPartitionId(), state);
    }

    @Override
    public BasePartition.DehydratedState get(String id) throws PartitionNotExistException {

        BasePartition.DehydratedState state = map().get(id);

        if (state == null) throw new PartitionNotExistException("unknown", id);

        return state;
    }

    @Override
    public boolean exists(String id) {

        return map().containsKey(id);
    }

    @Override
    public void remove(String id) throws PartitionNotExistException {

        BasePartition.DehydratedState state = map().remove(id);

        if (state == null) throw new PartitionNotExistException("unknown", id);
    }

    @Override
    public Iterator<String> recordIdIterator() {

        return map().keySet().iterator();
    }
}
