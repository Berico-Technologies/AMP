package amp.topology.global.impl;

import amp.topology.global.TopicConfiguration;
import amp.topology.global.TopicRegistry;
import amp.topology.global.exceptions.TopologyConfigurationNotExistException;
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

    Set<TopicConfiguration> registry = Sets.newCopyOnWriteArraySet();

    Object listenersLock = new Object();

    Set<Listener> listeners = Sets.newCopyOnWriteArraySet();

    @Override
    public TopicConfiguration get(String id) throws TopologyConfigurationNotExistException {

        Optional<TopicConfiguration> topicConfiguration = locate(id);

        if (!topicConfiguration.isPresent()) throw new TopologyConfigurationNotExistException();

        return topicConfiguration.get();
    }

    @Override
    public boolean exists(String id) {

        return locate(id).isPresent();
    }

    @Override
    public void register(TopicConfiguration topicConfiguration) throws Exception {

        synchronized (registryLock) {

            if (!locate(topicConfiguration.getId()).isPresent()){

                topicConfiguration.setup();

                // If an exception doesn't occur.
                registry.add(topicConfiguration);

                fireOnTopicRegistered(topicConfiguration);

                this.lastModified = System.currentTimeMillis();
            }
        }
    }

    void fireOnTopicRegistered(TopicConfiguration topicConfiguration) {

        for (Listener listener : listeners) listener.onTopicRegistered(topicConfiguration);
    }

    @Override
    public void unregister(String id) throws Exception {

        synchronized (registryLock) {

            Optional<TopicConfiguration> topic = locate(id);

            if (topic.isPresent()){

                topic.get().cleanup();

                // If an exception doesn't occur.
                registry.remove(topic);

                fireOnTopicUnRegistered(topic.get());

                this.lastModified = System.currentTimeMillis();
            }
            else {

                throw new TopologyConfigurationNotExistException();
            }
        }
    }

    void fireOnTopicUnRegistered(TopicConfiguration topicConfiguration) {

        for (Listener listener : listeners) listener.onTopicUnregistered(topicConfiguration);
    }

    @Override
    public Iterable<TopicConfiguration> entries() throws Exception {

        return Collections.unmodifiableCollection(registry);
    }

    @Override
    public long lastModified() {

        return lastModified;
    }

    @Override
    public void addListener(Listener listener) {

        synchronized (listenersLock) {

            listeners.add(listener);
        }
    }

    @Override
    public void removeListener(Listener listener) {

        synchronized (listenersLock) {

            listeners.remove(listener);
        }
    }

    /**
     * Get a TopicConfiguration by ID.
     * @param topicId ID of the Topic to retrieve.
     * @return TopicConfiguration of Null.
     */
    private Optional<TopicConfiguration> locate(String topicId){

        for (TopicConfiguration topicConfiguration : registry){

            if (topicConfiguration.getId().equals(topicId)) return Optional.of(topicConfiguration);
        }

        return Optional.absent();
    }
}
