package amp.topology.global.persistence;

import amp.topology.global.Topic;
import amp.topology.global.TopicRegistry;
import amp.topology.global.exceptions.TopicNotExistException;
import com.google.common.reflect.TypeToken;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class PersistentTopicIteratorTest {

    @Test
    @SuppressWarnings("unchecked")
    public void test_hasNext() throws TopicNotExistException {

        TopicRegistry registry = createMockTopicRegistry();

        Iterator<String> stringIterator1 = (Iterator<String>) mock(new TypeToken<Iterator<String>>(){}.getRawType());

        when(stringIterator1.hasNext()).thenReturn(true);

        PersistentTopicRegistry.PersistentTopicIterator iterator1 =
                new PersistentTopicRegistry.PersistentTopicIterator(registry, stringIterator1);

        assertTrue(iterator1.hasNext());

        Iterator<String> stringIterator2 = (Iterator<String>) mock(new TypeToken<Iterator<String>>(){}.getRawType());

        when(stringIterator2.hasNext()).thenReturn(false);

        PersistentTopicRegistry.PersistentTopicIterator iterator2 =
                new PersistentTopicRegistry.PersistentTopicIterator(registry, stringIterator2);

        assertFalse(iterator2.hasNext());
    }

    @Test
    public void test_next() throws TopicNotExistException {

        String expectedTopic1Id = "topic1";
        String expectedTopic2Id = "topic2";

        Topic expectedTopic1 = createMockTopic(expectedTopic1Id);
        Topic expectedTopic2 = createMockTopic(expectedTopic2Id);

        TopicRegistry registry = createMockTopicRegistry();

        List<String> entries = Arrays.asList(expectedTopic1Id, expectedTopic2Id);

        when(registry.get(expectedTopic1Id)).thenReturn(expectedTopic1);
        when(registry.get(expectedTopic2Id)).thenReturn(expectedTopic2);

        PersistentTopicRegistry.PersistentTopicIterator iterator =
                new PersistentTopicRegistry.PersistentTopicIterator(registry, entries.iterator());

        Topic actualTopic1 = iterator.next();
        Topic actualTopic2 = iterator.next();

        assertEquals(expectedTopic1, actualTopic1);
        assertEquals(expectedTopic2, actualTopic2);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void test_remove(){

        TopicRegistry registry = createMockTopicRegistry();

        Iterator<String> stringIterator = (Iterator<String>) mock(new TypeToken<Iterator<String>>(){}.getRawType());

        PersistentTopicRegistry.PersistentTopicIterator iterator =
                new PersistentTopicRegistry.PersistentTopicIterator(registry, stringIterator);

        iterator.remove();
    }


    static TopicRegistry createMockTopicRegistry(){

        return mock(TopicRegistry.class);
    }

    static Topic createMockTopic(String id){

        Topic topic = mock(Topic.class);

        when(topic.getTopicId()).thenReturn(id);

        return topic;
    }
}
