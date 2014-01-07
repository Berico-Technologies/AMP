package amp.topology.global;

import amp.topology.global.exceptions.TopologyConfigurationNotExistException;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Extend this class to test a TopicRegistry's compliance to the Topic's life cycle.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public abstract class TopicRegistryComplianceTest {

    protected abstract TopicRegistry getRegistry();

    @Test
    public void test_compliance__register() throws Exception {

        TopicRegistry topicRegistry = getRegistry();

        TopicConfiguration mockTopicConfiguration = mock(TopicConfiguration.class);

        topicRegistry.register(mockTopicConfiguration);

        verify(mockTopicConfiguration).setup();
    }

    @Test
    public void test_compliance__unregister() throws Exception {

        TopicRegistry topicRegistry = getRegistry();

        TopicConfiguration mockTopicConfiguration = mock(TopicConfiguration.class);

        when(mockTopicConfiguration.getId()).thenReturn("abc123");

        topicRegistry.register(mockTopicConfiguration);

        topicRegistry.unregister("abc123");

        verify(mockTopicConfiguration).cleanup();
    }

    @Test(expected = TopologyConfigurationNotExistException.class)
    public void test_compliance__unregister__throw_if_topic_not_exist() throws Exception {

        TopicRegistry topicRegistry = getRegistry();

        topicRegistry.unregister(UUID.randomUUID().toString());
    }
}
