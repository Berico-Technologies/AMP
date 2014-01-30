package amp.topology.global.impl;

import amp.topology.global.TopicRegistry;
import amp.topology.global.TopicRegistryComplianceTest;
import com.google.common.collect.Iterables;
import org.junit.Test;

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

        String SHOULD_EXIST = "test.BasicTopic";

        InMemoryTopicRegistry topicRegistry = new InMemoryTopicRegistry();

        BasicTopic topic = createMockTopic(SHOULD_EXIST);

        topicRegistry.register(topic);

        assertTrue(topicRegistry.exists(SHOULD_EXIST));

        assertFalse(topicRegistry.exists("SHOULD_NOT_EXIST"));
    }

    @Test
    public void test_get() throws Exception {

        String TOPIC_ID = "test.Topic2";

        InMemoryTopicRegistry topicRegistry = new InMemoryTopicRegistry();

        BasicTopic EXPECTED_TOPIC = createMockTopic(TOPIC_ID);

        topicRegistry.register(EXPECTED_TOPIC);

        amp.topology.global.Topic ACTUAL_TOPIC = topicRegistry.get(TOPIC_ID);

        assertEquals(EXPECTED_TOPIC, ACTUAL_TOPIC);
    }

    @Test
    public void test_entries() throws Exception {

        InMemoryTopicRegistry topicRegistry = new InMemoryTopicRegistry();

        BasicTopic TOPIC1 = createMockTopic("TOPIC_1");

        BasicTopic TOPIC2 = createMockTopic("TOPIC_2");

        BasicTopic TOPIC3 = createMockTopic("TOPIC_3");

        topicRegistry.register(TOPIC1);

        topicRegistry.register(TOPIC2);

        topicRegistry.register(TOPIC3);

        Iterable<amp.topology.global.Topic> ACTUAL_TOPICS = topicRegistry.entries();

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

        BasicTopic TOPIC = createMockTopic("amp.test.LastModified");

        topicRegistry.register(TOPIC);

        assertNotEquals(LAST_MODIFIED, topicRegistry.lastModified());

        LAST_MODIFIED = topicRegistry.lastModified();

        // The implementation is so fast that register/unregister are happening in the same millisecond!
        Thread.sleep(10);

        topicRegistry.unregister(TOPIC.getTopicId());

        assertNotEquals(LAST_MODIFIED, topicRegistry.lastModified());

        assertTrue(LAST_MODIFIED < topicRegistry.lastModified());
    }

    public static BasicTopic createMockTopic(String id){

        BasicTopic topic = mock(BasicTopic.class);

        when(topic.getTopicId()).thenReturn(id);

        return topic;
    }
}
