package amp.topology.global.persistence.mapdb;

import amp.topology.global.exceptions.ConnectorNotExistException;
import amp.topology.global.impl.BaseConnector;
import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class MapDbConnectorPersisterTest {

    @Test
    public void test_full_crud_scenario() throws ConnectorNotExistException {

        String expectedTopic = "MapDbConnectorPersisterTest_Topic1";
        String expectedConnector = expectedTopic + "_Connector1";
        String expectedDescription = "MapDbConnectorPersisterTest";
        String expectedPGroup = "pgroup";
        String expectedCGroup = "cgroup";

        BaseConnector.DehydratedState expectedState =
                new BaseConnector.DehydratedState(
                        BaseConnector.class,
                        expectedTopic,
                        expectedConnector,
                        expectedDescription,
                        expectedPGroup,
                        expectedCGroup,
                        new HashMap<String, String>());

        expectedState.getExtensionProperties().put("ext1", "value1");
        expectedState.getExtensionProperties().put("ext2", "value2");

        DB db = DBMaker.newTempFileDB().closeOnJvmShutdown().make();

        MapDbConnectorPersister persister = new MapDbConnectorPersister(db);

        persister.save(expectedState);

        assertTrue(persister.exists(expectedConnector));

        BaseConnector.DehydratedState actualState = persister.get(expectedConnector);

        assertEquals(expectedTopic, actualState.getTopicId());
        assertEquals(expectedDescription, actualState.getDescription());
        assertEquals(expectedPGroup, actualState.getProducerGroupId());
        assertEquals(expectedCGroup, actualState.getConsumerGroupId());
        assertEquals("value1", actualState.getExtensionProperties().get("ext1"));
        assertEquals("value2", actualState.getExtensionProperties().get("ext2"));

        persister.remove(expectedConnector);

        assertFalse(persister.exists(expectedConnector));
    }

    @Test(expected = ConnectorNotExistException.class)
    public void test_get_exception_thrown_if_record_not_exist() throws ConnectorNotExistException {

        DB db = DBMaker.newTempFileDB().closeOnJvmShutdown().make();

        MapDbConnectorPersister persister = new MapDbConnectorPersister(db);

        persister.get("nonexistent");
    }

    @Test(expected = ConnectorNotExistException.class)
    public void test_remove_exception_thrown_if_record_not_exist() throws ConnectorNotExistException {

        DB db = DBMaker.newTempFileDB().closeOnJvmShutdown().make();

        MapDbConnectorPersister persister = new MapDbConnectorPersister(db);

        persister.remove("nonexistent");
    }

    @Test
    public void test_recordIdIterator(){

        String expectedConnectorId1 = "MapDbConnectorPersisterTest_Topic4_Group1";
        String expectedConnectorId2 = "MapDbConnectorPersisterTest_Topic4_Group2";
        String expectedConnectorId3 = "MapDbConnectorPersisterTest_Topic4_Group3";

        List<String> allIds = Arrays.asList(expectedConnectorId1, expectedConnectorId2, expectedConnectorId3);

        BaseConnector.DehydratedState expectedState1 = createSampleConnector(expectedConnectorId1);
        BaseConnector.DehydratedState expectedState2 = createSampleConnector(expectedConnectorId2);
        BaseConnector.DehydratedState expectedState3 = createSampleConnector(expectedConnectorId3);

        DB db = DBMaker.newTempFileDB().closeOnJvmShutdown().make();

        MapDbConnectorPersister persister = new MapDbConnectorPersister(db);

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



    private static BaseConnector.DehydratedState createSampleConnector(String groupId){

        String expectedTopic = "MapDbConnectorPersisterTest_Topic4";
        String expectedConnector = groupId;
        String expectedDescription = "MapDbConnectorPersisterTest";
        String expectedPGroup = "pgroup";
        String expectedCGroup = "cgroup";

        BaseConnector.DehydratedState expectedState =
                new BaseConnector.DehydratedState(
                        BaseConnector.class,
                        expectedTopic,
                        expectedConnector,
                        expectedDescription,
                        expectedPGroup,
                        expectedCGroup,
                        new HashMap<String, String>());

        expectedState.getExtensionProperties().put("ext1", "value1");
        expectedState.getExtensionProperties().put("ext2", "value2");

        return expectedState;
    }
}
