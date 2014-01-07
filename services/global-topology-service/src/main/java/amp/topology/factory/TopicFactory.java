package amp.topology.factory;

import amp.topology.global.TopicConfiguration;

/**
 * Encapsulates the construction of topics, hiding as much detail as possible from the requester.
 *
 * The TopicFactory not only creates the topic, but also registers the topic with the TopicRegistry.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public interface TopicFactory {

    /**
     * Create a Topic with the provided specification.
     *
     * You are returned a copy of the TopicConfiguration so you can readily interact with it.  The Topic
     * will automatically be registered with the TopicRegistry (you don't need to do anything else).
     *
     * @param specification Specification of the topic.
     * @return TopicConfiguration.
     * @throws Exception an error occurring during the construction or registration process.
     */
    TopicConfiguration create(TopicSpecification specification) throws Exception;
}
