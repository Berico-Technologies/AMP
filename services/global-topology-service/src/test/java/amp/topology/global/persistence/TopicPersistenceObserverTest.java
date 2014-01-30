package amp.topology.global.persistence;

import amp.topology.global.PersistentTestBase;
import amp.topology.global.Topic;
import amp.topology.global.impl.BasicTopic;
import org.junit.Test;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class TopicPersistenceObserverTest extends PersistentTestBase {

    @Test
    public void test_onSaved(){

        BasicTopic topic = mock(BasicTopic.class);

        BasicTopic.DehydratedState expectedState = mock(BasicTopic.DehydratedState.class);

        when(topic.dehydrate()).thenReturn(expectedState);

        TopicPersistenceObserver observer = new TopicPersistenceObserver();

        observer.saveRequested(topic);

        verify(PersistenceManager.topics()).save(expectedState);
    }

    @Test
    public void test_getPersistable(){

        Topic shouldBeBasicTopic = mock(BasicTopic.class);

        BasicTopic shouldNotBeNull = TopicPersistenceObserver.getPersistable(shouldBeBasicTopic);

        assertNotNull(shouldNotBeNull);

        Topic shouldNotBeBasicTopic = mock(Topic.class);

        BasicTopic shouldBeNull = TopicPersistenceObserver.getPersistable(shouldNotBeBasicTopic);

        assertNull(shouldBeNull);
    }
}