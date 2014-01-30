package amp.topology.global.persistence.mapdb;

import amp.topology.global.exceptions.PartitionNotExistException;
import amp.topology.global.impl.BasePartition;
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
public class MapDbPartitionPersisterTest {

    @Test
    public void test_full_crud_scenario() throws PartitionNotExistException {

        String expectedTopic = "MapDbPartitionPersisterTest_Topic1";
        String expectedGroup = expectedTopic + "_Group1";
        String expectedPartition = expectedGroup + "_Partition1";
        String expectedDescription = "MapDbPartitionPersisterTest";

        BasePartition.DehydratedState expectedState =
                new BasePartition.DehydratedState(
                        BasePartition.class,
                        expectedTopic,
                        expectedDescription,
                        expectedGroup,
                        expectedPartition);

        expectedState.getExtensionProperties().put("ext1", "value1");
        expectedState.getExtensionProperties().put("ext2", "value2");

        DB db = DBMaker.newTempFileDB().closeOnJvmShutdown().make();

        MapDbPartitionPersister persister = new MapDbPartitionPersister(db);

        persister.save(expectedState);

        assertTrue(persister.exists(expectedPartition));

        BasePartition.DehydratedState actualState = persister.get(expectedPartition);

        assertEquals(expectedTopic, actualState.getTopicId());
        assertEquals(expectedDescription, actualState.getDescription());
        assertEquals(expectedGroup, actualState.getGroupId());
        assertEquals(expectedPartition, actualState.getPartitionId());
        assertEquals("value1", actualState.getExtensionProperties().get("ext1"));
        assertEquals("value2", actualState.getExtensionProperties().get("ext2"));

        persister.remove(expectedPartition);

        assertFalse(persister.exists(expectedPartition));
    }

    @Test(expected = PartitionNotExistException.class)
    public void test_get_exception_thrown_if_record_not_exist() throws PartitionNotExistException {

        DB db = DBMaker.newTempFileDB().closeOnJvmShutdown().make();

        MapDbPartitionPersister persister = new MapDbPartitionPersister(db);

        persister.get("nonexistent");
    }

    @Test(expected = PartitionNotExistException.class)
    public void test_remove_exception_thrown_if_record_not_exist() throws PartitionNotExistException {

        DB db = DBMaker.newTempFileDB().closeOnJvmShutdown().make();

        MapDbPartitionPersister persister = new MapDbPartitionPersister(db);

        persister.remove("nonexistent");
    }

    @Test
    public void test_recordIdIterator(){

        String expectedPartitionId1 = "MapDbPartitionPersisterTest_Topic4_Group1_Part1";
        String expectedPartitionId2 = "MapDbPartitionPersisterTest_Topic4_Group1_Part2";
        String expectedPartitionId3 = "MapDbPartitionPersisterTest_Topic4_Group1_Part3";

        List<String> allIds = Arrays.asList(expectedPartitionId1, expectedPartitionId2, expectedPartitionId3);

        BasePartition.DehydratedState expectedState1 = createSamplePartition(expectedPartitionId1);
        BasePartition.DehydratedState expectedState2 = createSamplePartition(expectedPartitionId2);
        BasePartition.DehydratedState expectedState3 = createSamplePartition(expectedPartitionId3);

        DB db = DBMaker.newTempFileDB().closeOnJvmShutdown().make();

        MapDbPartitionPersister persister = new MapDbPartitionPersister(db);

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



    private static BasePartition.DehydratedState createSamplePartition(String partitionId){

        String expectedTopic = "MapDbPartitionPersisterTest_Topic4";
        String expectedGroup = expectedTopic + "_Group1";
        String expectedPartition = partitionId;
        String expectedDescription = "MapDbPartitionPersisterTest";

        BasePartition.DehydratedState expectedState =
                new BasePartition.DehydratedState(
                        BasePartition.class,
                        expectedTopic,
                        expectedDescription,
                        expectedGroup,
                        expectedPartition);

        expectedState.getExtensionProperties().put("ext1", "value1");
        expectedState.getExtensionProperties().put("ext2", "value2");

        return expectedState;
    }
}
