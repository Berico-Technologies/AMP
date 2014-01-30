package amp.topology.global.impl;

import amp.topology.global.Topic;
import amp.topology.global.TopicRegistry;
import amp.topology.global.exceptions.TopicNotExistException;
import amp.topology.global.lifecycle.LifeCycleObserver;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Set;

/**
 * Keeps to BasicTopic graph in memory.
 *
 * Synchronization Policy: Writes are synchronized, reads are protected by an immutable list.
 *
 * The Registry is backed by a CopyOnWriteArraySet, which means mutations are made only by copying
 * the set.  Access to the list via (Iterator) guarantees an unmodifiable copy.  This does not mean
 * Read+Write operations are consistent.  It is possible to execute a read and while processing
 * that an item, have it mutated by a subsequent operation.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class InMemoryTopicRegistry implements TopicRegistry {

    long lastModified = -1;

    final Object registryLock = new Object();

    Set<amp.topology.global.Topic> registry = Sets.newCopyOnWriteArraySet();

    @Override
    public Topic get(String id) throws TopicNotExistException {

        Optional<amp.topology.global.Topic> topicConfiguration = locate(id);

        if (!topicConfiguration.isPresent()) throw new TopicNotExistException();

        return topicConfiguration.get();
    }

    @Override
    public boolean exists(String id) {

        return locate(id).isPresent();
    }

    @Override
    public void register(Topic topicConfiguration) throws Exception {

        synchronized (registryLock) {

            if (!locate(topicConfiguration.getTopicId()).isPresent()){

                if (BaseTopologyItem.class.isAssignableFrom(topicConfiguration.getClass()))
                    ((BaseTopologyItem)topicConfiguration).setup();

                // If an exception doesn't occur.
                registry.add(topicConfiguration);

                LifeCycleObserver.fireOnAdded(topicConfiguration);

                this.lastModified = System.currentTimeMillis();
            }
        }
    }

    @Override
    public void unregister(String id) throws Exception {

        synchronized (registryLock) {

            Optional<Topic> topic = locate(id);

            if (topic.isPresent()){

                if (BaseTopologyItem.class.isAssignableFrom(topic.get().getClass()))
                    ((BaseTopologyItem)topic.get()).cleanup();

                // If an exception doesn't occur.
                registry.remove(topic.get());

                LifeCycleObserver.fireOnRemoved(topic.get());

                this.lastModified = System.currentTimeMillis();
            }
            else {

                throw new TopicNotExistException();
            }
        }
    }

    @Override
    public Iterable<amp.topology.global.Topic> entries() throws Exception {

        return Collections.unmodifiableCollection(registry);
    }

    @Override
    public long lastModified() {

        return lastModified;
    }

    /**
     * Get a TopicConfiguration by ID.
     * @param topicId ID of the BasicTopic to retrieve.
     * @return TopicConfiguration of Null.
     */
    private Optional<amp.topology.global.Topic> locate(String topicId){

        for (Topic topic : registry){

            if (topic.getTopicId().equals(topicId)) return Optional.of(topic);
        }

        return Optional.absent();
    }
}
