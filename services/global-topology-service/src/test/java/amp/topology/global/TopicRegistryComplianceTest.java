package amp.topology.global;

import amp.topology.global.exceptions.TopicNotExistException;
import amp.topology.global.impl.BasicTopic;
import org.junit.Test;

import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * Extend this class to test a TopicRegistry's compliance to the BasicTopic's life cycle.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public abstract class TopicRegistryComplianceTest {

    protected abstract TopicRegistry getRegistry();

    @Test
    public void test_compliance__register() throws Exception {

        TopicRegistry topicRegistry = getRegistry();

        BasicTopic mockTopic = mock(BasicTopic.class);

        topicRegistry.register(mockTopic);

        verify(mockTopic).setup();
    }

    @Test
    public void test_compliance__unregister() throws Exception {

        TopicRegistry topicRegistry = getRegistry();

        BasicTopic mockTopic = mock(BasicTopic.class);

        when(mockTopic.getTopicId()).thenReturn("abc123");

        topicRegistry.register(mockTopic);

        topicRegistry.unregister("abc123");

        verify(mockTopic).cleanup();
    }

    @Test(expected = TopicNotExistException.class)
    public void test_compliance__unregister__throw_if_topic_not_exist() throws Exception {

        TopicRegistry topicRegistry = getRegistry();

        topicRegistry.unregister(UUID.randomUUID().toString());
    }
}
