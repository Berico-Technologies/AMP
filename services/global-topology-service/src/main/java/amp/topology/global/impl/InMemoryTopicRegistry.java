package amp.topology.global.impl;

import amp.topology.global.Topic;
import amp.topology.global.TopicRegistry;
import amp.topology.global.exceptions.TopicNotExistException;
import amp.topology.global.lifecycle.LifeCycleObservationManager;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Set;

/**
 * Keeps to Topic graph in memory.
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

    Object registryLock = new Object();

    Set<Topic> registry = Sets.newCopyOnWriteArraySet();

    @Override
    public Topic get(String id) throws TopicNotExistException {

        Optional<Topic> topicConfiguration = locate(id);

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

            if (!locate(topicConfiguration.getId()).isPresent()){

                topicConfiguration.setup();

                // If an exception doesn't occur.
                registry.add(topicConfiguration);

                LifeCycleObservationManager.fireOnAdded(topicConfiguration);

                this.lastModified = System.currentTimeMillis();
            }
        }
    }

    @Override
    public void unregister(String id) throws Exception {

        synchronized (registryLock) {

            Optional<Topic> topic = locate(id);

            if (topic.isPresent()){

                topic.get().cleanup();

                // If an exception doesn't occur.
                registry.remove(topic);

                LifeCycleObservationManager.fireOnRemoved(topic.get());

                this.lastModified = System.currentTimeMillis();
            }
            else {

                throw new TopicNotExistException();
            }
        }
    }

    @Override
    public Iterable<Topic> entries() throws Exception {

        return Collections.unmodifiableCollection(registry);
    }

    @Override
    public long lastModified() {

        return lastModified;
    }

    /**
     * Get a TopicConfiguration by ID.
     * @param topicId ID of the Topic to retrieve.
     * @return TopicConfiguration of Null.
     */
    private Optional<Topic> locate(String topicId){

        for (Topic topic : registry){

            if (topic.getId().equals(topicId)) return Optional.of(topic);
        }

        return Optional.absent();
    }
}
