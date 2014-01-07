package amp.topology.global;

import amp.topology.global.exceptions.TopologyConfigurationNotExistException;
import org.springframework.security.access.prepost.PreAuthorize;

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
    @PreAuthorize("hasRole('gts-snapshot-describe')")
    TopicConfiguration get(String id) throws TopologyConfigurationNotExistException;

    /**
     * Does a particular topic exist?
     * @param id of the topic configuration.
     * @return TRUE if a TopicConfiguration with that id exists.
     */
    @PreAuthorize("hasRole('gts-snapshot-describe')")
    boolean exists(String id);

    /**
     * Register a new TopicConfiguration with the registry.
     * @param topicConfiguration TopicConfiguration to register.
     * @throws Exception Thrown if an error occurs during the setup of the TopicConfiguration.
     */
    @PreAuthorize("hasRole('gts-snapshot-add')")
    void register(TopicConfiguration topicConfiguration) throws Exception;

    /**
     * Unregister a TopicConfiguration with the register.
     * @param id Id of the TopicConfiguration.
     * @throws TopologyConfigurationNotExistException Thrown if there is no such topic.
     * @throws Exception If an exception is encountered cleaning up the TopicConfiguration.
     */
    @PreAuthorize("hasRole('gts-snapshot-remove')")
    void unregister(String id) throws Exception;

    /**
     * Provide an iterable instance for navigating the list of registered Topics.
     * @return Iterable instance.
     * @throws Exception Could be caused by the underlying storage mechanism, particularly,
     * if you are holding onto the iterator for too long.
     */
    @PreAuthorize("hasRole('gts-snapshot-list')")
    Iterable<TopicConfiguration> entries() throws Exception;

    /**
     * Get the time the Topology was last modified.
     * @return Time last modified in millis.
     */
    @PreAuthorize("hasRole('gts-snapshot-info')")
    long lastModified();
}
