package amp.topology.global;

import amp.topology.global.exceptions.TopologyConfigurationNotExistException;

/**
 * Responsible for storing and providing access to the Topology tree.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public interface TopicRegistry {

    /**
     * Get a TopicConfiguration by Id.
     * @param id Id of the TopicConfiguration (though your implementation could support aliases...).
     * @return TopicConfiguration
     * @throws TopologyConfigurationNotExistException Thrown if there is no such topic.
     */
    TopicConfiguration get(String id) throws TopologyConfigurationNotExistException;

    /**
     * Does a particular topic exist?
     * @param id of the topic configuration.
     * @return TRUE if a TopicConfiguration with that id exists.
     */
    boolean exists(String id);

    /**
     * Register a new TopicConfiguration with the registry.
     * @param topicConfiguration TopicConfiguration to register.
     */
    void register(TopicConfiguration topicConfiguration);

    /**
     * Unregister a TopicConfiguration with the register.
     * @param id Id of the TopicConfiguration.
     * @throws TopologyConfigurationNotExistException Thrown if there is no such topic.
     */
    void unregister(String id) throws TopologyConfigurationNotExistException;

    /**
     * Provide an iterable instance for navigating the list of registered Topics.
     * @return Iterable instance.
     * @throws Exception Could be caused by the underlying storage mechanism, particularly,
     * if you are holding onto the iterator for too long.
     */
    Iterable<TopicConfiguration> entries() throws Exception;
}
