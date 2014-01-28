package amp.topology.global;

import amp.topology.global.exceptions.TopicNotExistException;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Responsible for storing and providing access to the Topology tree.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public interface TopicRegistry {

    /**
     * Get a Topic by Id.
     * @param id Id of the Topic (though your implementation could support aliases...).
     * @return Topic
     * @throws amp.topology.global.exceptions.TopicNotExistException Thrown if there is no such topic.
     */
    @PreAuthorize("hasRole('gts-snapshot-describe')")
    Topic get(String id) throws TopicNotExistException;

    /**
     * Does a particular topic exist?
     * @param id of the topic configuration.
     * @return TRUE if a Topic with that id exists.
     */
    @PreAuthorize("hasRole('gts-snapshot-describe')")
    boolean exists(String id);

    /**
     * Register a new Topic with the registry.
     * @param topic Topic to register.
     * @throws Exception Thrown if an error occurs during the setup of the Topic.
     */
    @PreAuthorize("hasRole('gts-snapshot-add')")
    void register(Topic topic) throws Exception;

    /**
     * Unregister a Topic with the register.
     * @param id Id of the Topic.
     * @throws amp.topology.global.exceptions.TopicNotExistException Thrown if there is no such topic.
     * @throws Exception If an exception is encountered cleaning up the Topic.
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
    Iterable<Topic> entries() throws Exception;

    /**
     * Get the time the Topology was last modified.
     * @return Time last modified in millis.
     */
    @PreAuthorize("hasRole('gts-snapshot-info')")
    long lastModified();
}
