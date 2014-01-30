package amp.topology.global.persistence.mapdb;

import amp.topology.global.exceptions.TopicNotExistException;
import amp.topology.global.impl.BasicTopic;
import amp.topology.global.persistence.TopologyStatePersister;
import org.mapdb.DB;

import java.util.Iterator;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class MapDbTopicPersister
        extends MapDbPersister<BasicTopic.DehydratedState>
        implements TopologyStatePersister.TopicStatePersister {

    public MapDbTopicPersister(DB configuredDatabase) {

        super(configuredDatabase);
    }

    @Override
    public String getCollectionName() {

        return "topics";
    }

    @Override
    public void save(BasicTopic.DehydratedState state) {

        map().put(state.getTopicId(), state);
    }

    @Override
    public BasicTopic.DehydratedState get(String id) throws TopicNotExistException {

        BasicTopic.DehydratedState state = map().get(id);

        if (state == null) throw new TopicNotExistException();

        return state;
    }

    @Override
    public boolean exists(String id) {

        return map().containsKey(id);
    }

    @Override
    public void remove(String id) throws TopicNotExistException {

        BasicTopic.DehydratedState state = map().remove(id);

        if (state == null) throw new TopicNotExistException();
    }

    @Override
    public Iterator<String> recordIdIterator() {

        return map().keySet().iterator();
    }
}