package amp.topology.global.persistence;

import amp.topology.global.Topic;
import amp.topology.global.TopicRegistry;
import amp.topology.global.exceptions.TopicNotExistException;
import amp.topology.global.impl.BaseTopic;

import java.util.Iterator;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class PersistentTopicRegistry implements TopicRegistry {

    @Override
    public Topic get(String id) throws TopicNotExistException {

        BaseTopic.DehydratedState state = PersistenceManager.topics().get(id);

        try {

            return Hydrater.hydrate(state);

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean exists(String id) {

        try {

            PersistenceManager.topics().get(id);

            return true;

        } catch (TopicNotExistException e) {

            return false;
        }
    }

    @Override
    public void register(Topic topic) throws Exception {

        if (BaseTopic.class.isAssignableFrom(topic.getClass())){

            ((BaseTopic)topic).setup();
        }

        topic.save();
    }

    @Override
    public void unregister(String id) throws Exception {

        ((BaseTopic)get(id)).cleanup();

        PersistenceManager.topics().remove(id);
    }

    @Override
    public Iterable<Topic> entries() throws Exception {

        return new PersistentTopicIterator(this, PersistenceManager.topics().recordIdIterator());
    }

    @Override
    public long lastModified() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public static class PersistentTopicIterator implements Iterator<Topic>, Iterable<Topic> {

        private TopicRegistry registry;

        private Iterator<String> topicIds;

        public PersistentTopicIterator(TopicRegistry registry, Iterator<String> topicIds) {
            this.registry = registry;
            this.topicIds = topicIds;
        }

        @Override
        public boolean hasNext() {

            return topicIds.hasNext();
        }

        @Override
        public Topic next() {

            String id = topicIds.next();

            try {

                return registry.get(id);

            } catch (TopicNotExistException e) {

                throw new RuntimeException(e);
            }
        }

        @Override
        public void remove() {

            throw new UnsupportedOperationException();
        }

        @Override
        public Iterator<Topic> iterator() {

            return this;
        }
    }
}
