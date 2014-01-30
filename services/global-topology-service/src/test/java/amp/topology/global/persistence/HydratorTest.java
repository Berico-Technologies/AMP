package amp.topology.global.persistence;

import amp.topology.global.*;
import amp.topology.global.impl.*;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class HydratorTest extends PersistentTestBase {

    @Test
    public void test_hydrate_topic() throws Exception {

        String expectedTopic = "HydratorTest_Topic1";
        String expectedConnector = expectedTopic + "_Connector1";
        String expectedGroup1 = expectedTopic + "_Group1";
        String expectedGroup2 = expectedTopic + "_Group2";
        String expectedDescription = "BaseConnectorTest";
        HashMap<String, String> expectedExtensions = Maps.newHashMap();

        BaseGroup.DehydratedState group1State = new BaseGroup.DehydratedState(
                TestProducerGroup.class, expectedTopic, expectedGroup1,
                expectedDescription, false, new ArrayList<String>());

        BaseGroup.DehydratedState group2State = new BaseGroup.DehydratedState(
                TestConsumerGroup.class, expectedTopic, expectedGroup2,
                expectedDescription, false, new ArrayList<String>());

        when(PersistenceManager.groups().get(expectedGroup1)).thenReturn(group1State);
        when(PersistenceManager.groups().get(expectedGroup2)).thenReturn(group2State);

        BaseConnector.DehydratedState connectorState = new BaseConnector.DehydratedState(
                TestConnector.class, expectedTopic, expectedConnector,
                expectedDescription, expectedGroup1, expectedGroup2, expectedExtensions);

        when(PersistenceManager.connectors().get(expectedConnector)).thenReturn(connectorState);

        BasicTopic.DehydratedState state =
                new BasicTopic.DehydratedState(
                        BasicTopic.class, expectedTopic, expectedDescription,
                        Arrays.asList(expectedGroup1), Arrays.asList(expectedGroup2), Arrays.asList(expectedConnector));


        Topic topic = Hydrater.hydrate(state);

        assertEquals(expectedTopic, topic.getTopicId());
        assertEquals(expectedDescription, topic.getDescription());
        assertEquals(BasicTopic.class, topic.getClass());

        validateGroup(topic.getProducerGroup(expectedGroup1), expectedTopic,
                expectedGroup1, expectedDescription, TestProducerGroup.class);

        validateGroup(topic.getConsumerGroup(expectedGroup2), expectedTopic,
                expectedGroup2, expectedDescription, TestConsumerGroup.class);

        validateConnector(
                topic.getConnector(expectedConnector),
                expectedTopic,
                expectedConnector,
                expectedDescription,
                topic.getProducerGroup(expectedGroup1),
                topic.getConsumerGroup(expectedGroup2),
                TestConnector.class);
    }

    @Test
    public void test_hydrate_group() throws Exception {

        String expectedTopic = "HydratorTest_Topic2";
        String expectedGroup = expectedTopic + "_Group1";
        String expectedDescription = "HydratorTest";
        String expectedPartition1Id = expectedGroup + "_Part1";
        String expectedPartition2Id = expectedGroup + "_Part2";

        BasePartition.DehydratedState partition1State =
                new BasePartition.DehydratedState(
                        TestPartition.class, expectedTopic, expectedDescription, expectedGroup, expectedPartition1Id);

        BasePartition.DehydratedState partition2State =
                new BasePartition.DehydratedState(
                        TestPartition.class, expectedTopic, expectedDescription, expectedGroup, expectedPartition2Id);

        when(PersistenceManager.partitions().get(expectedPartition1Id)).thenReturn(partition1State);

        when(PersistenceManager.partitions().get(expectedPartition2Id)).thenReturn(partition2State);

        BaseGroup.DehydratedState state =
                new BaseGroup.DehydratedState(
                        TestProducerGroup.class,
                        expectedTopic,
                        expectedGroup,
                        expectedDescription,
                        false,
                        Arrays.asList(
                                expectedPartition1Id,
                                expectedPartition2Id));

        Group group = Hydrater.hydrate(state);

        validateGroup(group, expectedTopic, expectedGroup, expectedDescription, TestProducerGroup.class);

        assertEquals(2, group.getPartitions().size());

        Partition actualPartition1 = group.getPartition(expectedPartition1Id);
        Partition actualPartition2 = group.getPartition(expectedPartition2Id);

        validatePartition(
                actualPartition1, expectedTopic, expectedGroup,
                expectedPartition1Id, expectedDescription, TestPartition.class);

        validatePartition(
                actualPartition2, expectedTopic, expectedGroup,
                expectedPartition2Id, expectedDescription, TestPartition.class);
    }


    @Test
    public void test_hydrate_partition() throws Exception {

        String expectedTopic = "HydratorTest_Topic3";
        String expectedGroup = expectedTopic + "_Group1";
        String expectedPartition = expectedGroup + "_Partition1";
        String expectedDescription = "HydratorTest";

        BasePartition.DehydratedState state =
                new BasePartition.DehydratedState(
                        TestPartition.class, expectedTopic, expectedDescription, expectedGroup, expectedPartition);

        Partition partition = Hydrater.hydrate(state);

        validatePartition(
                partition, expectedTopic, expectedGroup, expectedPartition, expectedDescription, TestPartition.class);
    }

    @Test
    public void test_hydrate_connector() throws Exception {

        String expectedTopic = "HydratorTest_Topic4";
        String expectedConnector = expectedTopic + "_Connector1";
        String expectedGroup1 = expectedTopic + "_Group1";
        String expectedGroup2 = expectedTopic + "_Group2";
        String expectedDescription = "BaseConnectorTest";
        HashMap<String, String> expectedExtensions = Maps.newHashMap();

        BaseGroup.DehydratedState group1State = new BaseGroup.DehydratedState(
                TestProducerGroup.class, expectedTopic, expectedGroup1,
                expectedDescription, false, new ArrayList<String>());

        BaseGroup.DehydratedState group2State = new BaseGroup.DehydratedState(
                TestConsumerGroup.class, expectedTopic, expectedGroup2,
                expectedDescription, false, new ArrayList<String>());

        when(PersistenceManager.groups().get(expectedGroup1)).thenReturn(group1State);
        when(PersistenceManager.groups().get(expectedGroup2)).thenReturn(group2State);

        BaseConnector.DehydratedState state = new BaseConnector.DehydratedState(
                TestConnector.class, expectedTopic, expectedConnector,
                expectedDescription, expectedGroup1, expectedGroup2, expectedExtensions);

        Connector connector = Hydrater.hydrate(state);

        assertEquals(expectedTopic, connector.getTopicId());
        assertEquals(expectedConnector, connector.getConnectorId());
        assertEquals(expectedDescription, connector.getDescription());

        validateGroup(connector.getProducerGroup(), expectedTopic,
                expectedGroup1, expectedDescription, TestProducerGroup.class);

        validateGroup(connector.getConsumerGroup(), expectedTopic,
                expectedGroup2, expectedDescription, TestConsumerGroup.class);
    }

    @Test
    public void test_hydrate_connector_using_topic_state() throws Exception {

        String expectedTopic = "HydratorTest_Topic5";
        String expectedConnector = expectedTopic + "_Connector1";
        String expectedGroup1 = expectedTopic + "_Group1";
        String expectedGroup2 = expectedTopic + "_Group2";
        String expectedDescription = "BaseConnectorTest";
        HashMap<String, String> expectedExtensions = Maps.newHashMap();

        ProducerGroup expectedPGroup = createMockProducerGroup(expectedGroup1);
        ConsumerGroup expectedCGroup = createMockConsumerGroup(expectedGroup2);

        Topic topic = mock(Topic.class);

        when(topic.getProducerGroup(expectedGroup1)).thenReturn(expectedPGroup);
        when(topic.getConsumerGroup(expectedGroup2)).thenReturn(expectedCGroup);

        BaseConnector.DehydratedState state = new BaseConnector.DehydratedState(
                TestConnector.class, expectedTopic, expectedConnector,
                expectedDescription, expectedGroup1, expectedGroup2, expectedExtensions);

        Connector connector = Hydrater.hydrate(state, topic);

        validateConnector(
                connector, expectedTopic, expectedConnector,
                expectedDescription, expectedPGroup, expectedCGroup, TestConnector.class);

        verify(PersistenceManager.groups(), never()).get(expectedGroup1);
        verify(PersistenceManager.groups(), never()).get(expectedGroup2);
    }

    public void validateConnector(
            Connector connector,
            String expectedTopic,
            String expectedConnector,
            String expectedDescription,
            Group expectedPGroup,
            Group expectedCGroup,
            Class<? extends Connector> expectedConnectorClass){

        assertEquals(expectedTopic, connector.getTopicId());
        assertEquals(expectedConnector, connector.getConnectorId());
        assertEquals(expectedDescription, connector.getDescription());

        assertEquals(expectedPGroup, connector.getProducerGroup());
        assertEquals(expectedCGroup, connector.getConsumerGroup());
    }

    public void validateGroup(
            Group group,
            String expectedTopic,
            String expectedGroup,
            String expectedDescription,
            Class<? extends Group> expectedGroupClass){

        assertEquals(expectedTopic, group.getTopicId());
        assertEquals(expectedGroup, group.getGroupId());
        assertEquals(expectedDescription, group.getDescription());
        assertEquals(expectedGroupClass, group.getClass());
    }

    public void validatePartition(
            Partition partition,
            String expectedTopic,
            String expectedGroup,
            String expectedPartition,
            String expectedDescription,
            Class<? extends Partition> expectedPartitionClass){

        assertEquals(expectedTopic, partition.getTopicId());
        assertEquals(expectedGroup, partition.getGroupId());
        assertEquals(expectedPartition, partition.getPartitionId());
        assertEquals(expectedDescription, partition.getDescription());
        assertEquals(expectedPartitionClass, partition.getClass());
    }

}
