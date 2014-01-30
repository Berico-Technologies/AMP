package amp.topology.global.persistence;

import amp.topology.global.PersistentTestBase;
import amp.topology.global.Topic;
import amp.topology.global.exceptions.TopicNotExistException;
import amp.topology.global.impl.BasicTopic;
import amp.topology.global.lifecycle.LifeCycleListener;
import amp.topology.global.lifecycle.LifeCycleObserver;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class PersistentTopicRegistryTest extends PersistentTestBase {

    @Test
    public void test_get() throws Exception {

        String expectedTopicId = "PersistentTopicRegistryTest_Topic1";
        String expectedDescription = "PersistentTopicRegistryTest_Topic1";

        BasicTopic.DehydratedState expectedState =
                new BasicTopic.DehydratedState(
                        BasicTopic.class,
                        expectedTopicId,
                        expectedDescription,
                        new ArrayList<String>(),
                        new ArrayList<String>(),
                        new ArrayList<String>());

        when(PersistenceManager.topics().get(expectedTopicId)).thenReturn(expectedState);

        PersistentTopicRegistry registry = new PersistentTopicRegistry();

        assertNotNull(registry.get(expectedTopicId));
    }

    @Test(expected = TopicNotExistException.class)
    public void test_get_exception_thrown_if_topic_not_exists() throws TopicNotExistException {

        String shouldNotExist = "nonexistent";

        when(PersistenceManager.topics().get(shouldNotExist)).thenThrow(new TopicNotExistException());

        PersistentTopicRegistry registry = new PersistentTopicRegistry();

        registry.get(shouldNotExist);
    }

    @Test
    public void test_exists(){

        String expectedToExist = "PersistentTopicRegistryTest_Topic3";

        when(PersistenceManager.topics().exists(expectedToExist)).thenReturn(true);

        PersistentTopicRegistry registry = new PersistentTopicRegistry();

        assertTrue(registry.exists(expectedToExist));
        assertFalse(registry.exists("nonexistent"));
    }

    @Test
    public void test_register() throws Exception {

        LifeCycleListener.TopicListener listener = mock(LifeCycleListener.TopicListener.class);

        LifeCycleObserver.addListener(listener);

        String expectedTopic = "PersistentTopicRegistryTest_Topic4";

        BasicTopic topic = mock(BasicTopic.class);

        BasicTopic.DehydratedState expectedState = mock(BasicTopic.DehydratedState.class);

        when(topic.dehydrate()).thenReturn(expectedState);

        when(topic.getTopicId()).thenReturn(expectedTopic);

        PersistentTopicRegistry registry = new PersistentTopicRegistry();

        registry.register(topic);

        verify(topic).setup();

        verify(PersistenceManager.topics()).save(expectedState);

        verify(listener).onAdded(topic);
    }

    @Test
    public void test_unregister() throws Exception {

        LifeCycleListener.TopicListener listener = mock(LifeCycleListener.TopicListener.class);

        LifeCycleObserver.addListener(listener);

        String expectedTopic = "PersistentTopicRegistryTest_Topic5";

        BasicTopic basicTopic = mock(BasicTopic.class);

        when(basicTopic.getTopicId()).thenReturn(expectedTopic);

        PersistentTopicRegistry registry = spy( new PersistentTopicRegistry() );

        doReturn(basicTopic).when(registry).get(expectedTopic);

        registry.unregister(expectedTopic);

        verify(basicTopic).cleanup();

        verify(PersistenceManager.topics()).remove(expectedTopic);

        verify(listener).onRemoved(basicTopic);
    }

    @Test(expected = TopicNotExistException.class)
    public void test_unregister_exception_thrown_if_topic_not_exists() throws Exception {

        String shouldNotExist = "nonexistent";

        when(PersistenceManager.topics().get(shouldNotExist)).thenThrow(new TopicNotExistException());

        PersistentTopicRegistry registry = new PersistentTopicRegistry();

        registry.unregister(shouldNotExist);
    }

    @Test
    public void test_entries() throws Exception {

        String expectedTopic1Id = "PersistentTopicRegistryTest_Topic7";
        String expectedTopic2Id = "PersistentTopicRegistryTest_Topic8";

        List<String> topicIds = Arrays.asList(expectedTopic1Id, expectedTopic2Id);

        when(PersistenceManager.topics().recordIdIterator()).thenReturn(topicIds.iterator());

        PersistentTopicRegistry registry = spy( new PersistentTopicRegistry() );

        Topic expectedTopic1 = mock(Topic.class);
        Topic expectedTopic2 = mock(Topic.class);

        doReturn(expectedTopic1).when(registry).get(expectedTopic1Id);
        doReturn(expectedTopic2).when(registry).get(expectedTopic2Id);

        Iterable<Topic> topics = registry.entries();

        for(Topic topic : topics){

            assertTrue(topic.equals(expectedTopic1) || topic.equals(expectedTopic2));
        }
    }

}
