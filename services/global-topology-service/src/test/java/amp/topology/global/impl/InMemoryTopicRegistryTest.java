package amp.topology.global.impl;

import amp.topology.global.TopicConfiguration;
import amp.topology.global.TopicRegistry;
import amp.topology.global.TopicRegistryComplianceTest;
import com.google.common.collect.Iterables;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class InMemoryTopicRegistryTest extends TopicRegistryComplianceTest {

    @Override
    protected TopicRegistry getRegistry() {

        return new InMemoryTopicRegistry();
    }

    @Test
    public void test_exists() throws Exception {

        String SHOULD_EXIST = "test.Topic";

        InMemoryTopicRegistry topicRegistry = new InMemoryTopicRegistry();

        TopicConfiguration topic = createMockTopic(SHOULD_EXIST);

        topicRegistry.register(topic);

        assertTrue(topicRegistry.exists(SHOULD_EXIST));

        assertFalse(topicRegistry.exists("SHOULD_NOT_EXIST"));
    }

    @Test
    public void test_get() throws Exception {

        String TOPIC_ID = "test.Topic2";

        InMemoryTopicRegistry topicRegistry = new InMemoryTopicRegistry();

        TopicConfiguration EXPECTED_TOPIC = createMockTopic(TOPIC_ID);

        topicRegistry.register(EXPECTED_TOPIC);

        TopicConfiguration ACTUAL_TOPIC = topicRegistry.get(TOPIC_ID);

        assertEquals(EXPECTED_TOPIC, ACTUAL_TOPIC);
    }

    @Test
    public void test_entries() throws Exception {

        InMemoryTopicRegistry topicRegistry = new InMemoryTopicRegistry();

        TopicConfiguration TOPIC1 = createMockTopic("TOPIC_1");

        TopicConfiguration TOPIC2 = createMockTopic("TOPIC_2");

        TopicConfiguration TOPIC3 = createMockTopic("TOPIC_3");

        topicRegistry.register(TOPIC1);

        topicRegistry.register(TOPIC2);

        topicRegistry.register(TOPIC3);

        Iterable<TopicConfiguration> ACTUAL_TOPICS = topicRegistry.entries();

        assertEquals(3, Iterables.size(ACTUAL_TOPICS));

        assertTrue(Iterables.contains(ACTUAL_TOPICS, TOPIC1));

        assertTrue(Iterables.contains(ACTUAL_TOPICS, TOPIC2));

        assertTrue(Iterables.contains(ACTUAL_TOPICS, TOPIC3));
    }

    @Test
    public void test_lastModified() throws Exception {

        InMemoryTopicRegistry topicRegistry = new InMemoryTopicRegistry();

        long LAST_MODIFIED = topicRegistry.lastModified();

        assertTrue(LAST_MODIFIED < 0);

        TopicConfiguration TOPIC = createMockTopic("amp.test.LastModified");

        topicRegistry.register(TOPIC);

        assertNotEquals(LAST_MODIFIED, topicRegistry.lastModified());

        LAST_MODIFIED = topicRegistry.lastModified();

        // The implementation is so fast that register/unregister are happening in the same millisecond!
        Thread.sleep(10);

        topicRegistry.unregister(TOPIC.getId());

        assertNotEquals(LAST_MODIFIED, topicRegistry.lastModified());

        assertTrue(LAST_MODIFIED < topicRegistry.lastModified());
    }

    @Test
    public void test_listeners() throws Exception {

        InMemoryTopicRegistry topicRegistry = new InMemoryTopicRegistry();

        topicRegistry.listeners = spy(topicRegistry.listeners);

        TopicRegistry.Listener listener = mock(TopicRegistry.Listener.class);

        topicRegistry.addListener(listener);

        verify(topicRegistry.listeners).add(listener);

        TopicConfiguration topic1 = createMockTopic("abc123");

        TopicConfiguration topic2 = createMockTopic("abc345");

        topicRegistry.register(topic1);

        topicRegistry.register(topic2);

        verify(listener).onTopicRegistered(topic1);

        verify(listener).onTopicRegistered(topic2);

        topicRegistry.unregister(topic1.getId());

        verify(listener).onTopicUnregistered(topic1);

        topicRegistry.removeListener(listener);

        topicRegistry.unregister(topic2.getId());

        verifyNoMoreInteractions(listener);
    }

    public static TopicConfiguration createMockTopic(String id){

        TopicConfiguration topic = mock(TopicConfiguration.class);

        when(topic.getId()).thenReturn(id);

        return topic;
    }
}
