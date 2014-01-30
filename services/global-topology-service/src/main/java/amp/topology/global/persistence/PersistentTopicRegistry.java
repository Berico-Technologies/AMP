package amp.topology.global.persistence;

import amp.topology.global.Topic;
import amp.topology.global.TopicRegistry;
import amp.topology.global.exceptions.TopicNotExistException;
import amp.topology.global.impl.BasicTopic;
import amp.topology.global.lifecycle.LifeCycleObserver;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class PersistentTopicRegistry implements TopicRegistry {

    protected AtomicLong lastModified = new AtomicLong();

    @Override
    public Topic get(String id) throws TopicNotExistException {

        BasicTopic.DehydratedState state = PersistenceManager.topics().get(id);

        try {

            return Hydrater.hydrate(state);

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean exists(String id) {

        return PersistenceManager.topics().exists(id);
    }

    @Override
    public void register(Topic topic) throws Exception {

        if (BasicTopic.class.isAssignableFrom(topic.getClass())){

            BasicTopic basicTopic = (BasicTopic)topic;

            basicTopic.setup();

            PersistenceManager.topics().save(basicTopic.dehydrate());
        }

        LifeCycleObserver.fireOnAdded(topic);

        lastModified.incrementAndGet();
    }

    @Override
    public void unregister(String id) throws Exception {

        Topic topic = get(id);

        if (BasicTopic.class.isAssignableFrom(topic.getClass())){

            BasicTopic basicTopic = (BasicTopic)topic;

            basicTopic.cleanup();

            PersistenceManager.topics().remove(basicTopic.getTopicId());
        }

        LifeCycleObserver.fireOnRemoved(topic);

        lastModified.incrementAndGet();
    }

    @Override
    public Iterable<Topic> entries() throws Exception {

        return new PersistentTopicIterable(this);
    }

    @Override
    public long lastModified() {

        return lastModified.get();
    }

    public static class PersistentTopicIterable implements Iterable<Topic> {

        private TopicRegistry registry;

        public PersistentTopicIterable(TopicRegistry registry) {
            this.registry = registry;
        }

        @Override
        public Iterator<Topic> iterator() {

            return new PersistentTopicIterator(registry, PersistenceManager.topics().recordIdIterator());
        }
    }

    public static class PersistentTopicIterator implements Iterator<Topic> {

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
    }
}
