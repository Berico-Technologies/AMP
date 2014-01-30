package amp.topology.global.persistence.mapdb;

import amp.topology.global.exceptions.TopologyGroupNotExistException;
import amp.topology.global.impl.BaseGroup;
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
public class MapDbGroupPersistenceTest {

    @Test
    public void test_full_crud_scenario() throws TopologyGroupNotExistException {

        String expectedTopic = "MapDbGroupPersisterTest_Topic1";
        String expectedGroup = expectedTopic + "_Group1";
        String expectedDescription = "MapDbGroupPersisterTest";
        List<String> expectedPartitions = Arrays.asList("part1", "part2");

        BaseGroup.DehydratedState expectedState =
                new BaseGroup.DehydratedState(
                        BaseGroup.class, expectedTopic, expectedGroup, expectedDescription, false, expectedPartitions);

        expectedState.getExtensionProperties().put("ext1", "value1");
        expectedState.getExtensionProperties().put("ext2", "value2");

        DB db = DBMaker.newTempFileDB().closeOnJvmShutdown().make();

        MapDbGroupPersister persister = new MapDbGroupPersister(db);

        persister.save(expectedState);

        assertTrue(persister.exists(expectedGroup));

        BaseGroup.DehydratedState actualState = persister.get(expectedGroup);

        assertEquals(expectedTopic, actualState.getTopicId());
        assertEquals(expectedDescription, actualState.getDescription());
        assertEquals(expectedGroup, actualState.getGroupId());
        assertEquals(false, actualState.isConsumerGroup());
        assertEquals(2, actualState.getPartitionIds().size());
        assertTrue(actualState.getPartitionIds().containsAll(expectedPartitions));
        assertEquals("value1", actualState.getExtensionProperties().get("ext1"));
        assertEquals("value2", actualState.getExtensionProperties().get("ext2"));

        persister.remove(expectedGroup);

        assertFalse(persister.exists(expectedGroup));
    }

    @Test(expected = TopologyGroupNotExistException.class)
    public void test_get_exception_thrown_if_record_not_exist() throws TopologyGroupNotExistException {

        DB db = DBMaker.newTempFileDB().closeOnJvmShutdown().make();

        MapDbGroupPersister persister = new MapDbGroupPersister(db);

        persister.get("nonexistent");
    }

    @Test(expected = TopologyGroupNotExistException.class)
    public void test_remove_exception_thrown_if_record_not_exist() throws TopologyGroupNotExistException {

        DB db = DBMaker.newTempFileDB().closeOnJvmShutdown().make();

        MapDbGroupPersister persister = new MapDbGroupPersister(db);

        persister.remove("nonexistent");
    }

    @Test
    public void test_recordIdIterator(){

        String expectedGroupId1 = "MapDbGroupPersisterTest_Topic4_Group1";
        String expectedGroupId2 = "MapDbGroupPersisterTest_Topic4_Group2";
        String expectedGroupId3 = "MapDbGroupPersisterTest_Topic4_Group3";

        List<String> allIds = Arrays.asList(expectedGroupId1, expectedGroupId2, expectedGroupId3);

        BaseGroup.DehydratedState expectedState1 = createSampleGroup(expectedGroupId1);
        BaseGroup.DehydratedState expectedState2 = createSampleGroup(expectedGroupId2);
        BaseGroup.DehydratedState expectedState3 = createSampleGroup(expectedGroupId3);

        DB db = DBMaker.newTempFileDB().closeOnJvmShutdown().make();

        MapDbGroupPersister persister = new MapDbGroupPersister(db);

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



    private static BaseGroup.DehydratedState createSampleGroup(String groupId){

        String expectedTopic = "MapDbGroupPersisterTest_Topic4";
        String expectedGroup = groupId;
        String expectedDescription = "MapDbGroupPersisterTest";
        List<String> expectedPartitions = Arrays.asList("part1", "part2");

        BaseGroup.DehydratedState expectedState =
                new BaseGroup.DehydratedState(
                        BaseGroup.class, expectedTopic, expectedGroup, expectedDescription, false, expectedPartitions);

        expectedState.getExtensionProperties().put("ext1", "value1");
        expectedState.getExtensionProperties().put("ext2", "value2");

        return expectedState;
    }
}
