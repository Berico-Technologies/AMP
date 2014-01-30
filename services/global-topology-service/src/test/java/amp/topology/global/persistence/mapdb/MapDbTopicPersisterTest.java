package amp.topology.global.persistence.mapdb;

import amp.topology.global.exceptions.TopicNotExistException;
import amp.topology.global.impl.BasicTopic;
import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class MapDbTopicPersisterTest {

    @Test
    public void test_full_crud_scenario() throws TopicNotExistException {

        String expectedTopic = "MapDbTopicPersisterTest_Topic1";
        String expectedDescription = "MapDbTopicPersisterTest";
        List<String> expectedPGroups = Arrays.asList("pgroup1", "pgroup2");
        List<String> expectedCGroups = Arrays.asList("cgroup1", "cgroup2");
        List<String> expectedConnectors = Arrays.asList("connector1");

        BasicTopic.DehydratedState expectedState =
                new BasicTopic.DehydratedState(
                        BasicTopic.class,
                        expectedTopic,
                        expectedDescription,
                        expectedPGroups,
                        expectedCGroups,
                        expectedConnectors);

        expectedState.getExtensionProperties().put("ext1", "value1");
        expectedState.getExtensionProperties().put("ext2", "value2");

        DB db = DBMaker.newTempFileDB().closeOnJvmShutdown().make();

        MapDbTopicPersister persister = new MapDbTopicPersister(db);

        persister.save(expectedState);

        assertTrue(persister.exists(expectedTopic));

        BasicTopic.DehydratedState actualState = persister.get(expectedTopic);

        assertEquals(expectedTopic, actualState.getTopicId());
        assertEquals(expectedDescription, actualState.getDescription());
        assertEquals(2, actualState.getProducerGroupIds().size());
        assertEquals(2, actualState.getConsumerGroupIds().size());
        assertEquals(1, actualState.getConnectorIds().size());
        assertTrue(actualState.getProducerGroupIds().containsAll(expectedPGroups));
        assertTrue(actualState.getConsumerGroupIds().containsAll(expectedCGroups));
        assertTrue(actualState.getConnectorIds().containsAll(expectedConnectors));
        assertEquals("value1", actualState.getExtensionProperties().get("ext1"));
        assertEquals("value2", actualState.getExtensionProperties().get("ext2"));

        persister.remove(expectedTopic);

        assertFalse(persister.exists(expectedTopic));
    }

    @Test(expected = TopicNotExistException.class)
    public void test_get_exception_thrown_if_record_not_exist() throws TopicNotExistException {

        DB db = DBMaker.newTempFileDB().closeOnJvmShutdown().make();

        MapDbTopicPersister persister = new MapDbTopicPersister(db);

        persister.get("nonexistent");
    }

    @Test(expected = TopicNotExistException.class)
    public void test_remove_exception_thrown_if_record_not_exist() throws TopicNotExistException {

        DB db = DBMaker.newTempFileDB().closeOnJvmShutdown().make();

        MapDbTopicPersister persister = new MapDbTopicPersister(db);

        persister.remove("nonexistent");
    }

    @Test
    public void test_recordIdIterator(){

        String expectedTopicId1 = "MapDbTopicPersisterTest_Topic4.1";
        String expectedTopicId2 = "MapDbTopicPersisterTest_Topic4.2";
        String expectedTopicId3 = "MapDbTopicPersisterTest_Topic4.3";

        List<String> allIds = Arrays.asList(expectedTopicId1, expectedTopicId2, expectedTopicId3);

        BasicTopic.DehydratedState expectedState1 = createSampleTopic(expectedTopicId1);
        BasicTopic.DehydratedState expectedState2 = createSampleTopic(expectedTopicId2);
        BasicTopic.DehydratedState expectedState3 = createSampleTopic(expectedTopicId3);

        DB db = DBMaker.newTempFileDB().closeOnJvmShutdown().make();

        MapDbTopicPersister persister = new MapDbTopicPersister(db);

        persister.save(expectedState1);
        persister.save(expectedState2);
        persister.save(expectedState3);

        Iterator<String> idIterator = persister.recordIdIterator();

        int actualCount = 0;

        while(idIterator.hasNext()){

            assertTrue(allIds.contains(idIterator.next()));

            actualCount++;
        }

        assertEquals(allIds.size(), actualCount);
    }



    private static BasicTopic.DehydratedState createSampleTopic(String topicId){

        String expectedTopic = topicId;
        String expectedDescription = "MapDbTopicPersisterTest";
        List<String> expectedPGroups = Arrays.asList("pgroup1", "pgroup2");
        List<String> expectedCGroups = Arrays.asList("cgroup1", "cgroup2");
        List<String> expectedConnectors = Arrays.asList("connector1");

        BasicTopic.DehydratedState expectedState =
                new BasicTopic.DehydratedState(
                        BasicTopic.class,
                        expectedTopic,
                        expectedDescription,
                        expectedPGroups,
                        expectedCGroups,
                        expectedConnectors);

        expectedState.getExtensionProperties().put("ext1", "value1");
        expectedState.getExtensionProperties().put("ext2", "value2");

        return expectedState;
    }

}
