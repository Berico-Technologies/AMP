package amp.topology.global.impl;

import amp.topology.global.PersistentTestBase;
import amp.topology.global.exceptions.PartitionAlreadyExistsException;
import amp.topology.global.exceptions.PartitionNotExistException;
import amp.topology.global.lifecycle.LifeCycleListener;
import amp.topology.global.lifecycle.LifeCycleObserver;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class BaseGroupTest extends PersistentTestBase {

    @Test
    public void test_save(){

        LifeCycleListener.GroupListener listener = mock(LifeCycleListener.GroupListener.class);

        LifeCycleObserver.addListener(listener);

        String expectedTopic = "BaseGroupTest_Topic1";
        String expectedGroup = expectedTopic + "_Group1";
        String expectedDescription = "BaseGroupTest";
        String expectedPartition1Id = expectedGroup + "_Part1";
        String expectedPartition2Id = expectedGroup + "_Part2";

        TestPartition partition1 = createMockPartition(expectedPartition1Id);
        TestPartition partition2 = createMockPartition(expectedPartition2Id);

        TestProducerGroup group = new TestProducerGroup();

        group.setTopicId(expectedTopic);
        group.setGroupId(expectedGroup);
        group.setDescription(expectedDescription);
        group.setPartitions(Arrays.asList(partition1, partition2));

        group.save();

        verify(partition1).save();
        verify(partition2).save();

        verify(listener).saveRequested(group);
    }

    @Test
    public void test_save_verify_aggregates_are_not_saved(){

        String expectedTopic = "BaseGroupTest_Topic1";
        String expectedGroup = expectedTopic + "_Group1";
        String expectedDescription = "BaseGroupTest";
        String expectedPartition1Id = expectedGroup + "_Part1";
        String expectedPartition2Id = expectedGroup + "_Part2";

        TestPartition partition1 = createMockPartition(expectedPartition1Id);
        TestPartition partition2 = createMockPartition(expectedPartition2Id);

        TestProducerGroup group = new TestProducerGroup();

        group.setTopicId(expectedTopic);
        group.setGroupId(expectedGroup);
        group.setDescription(expectedDescription);
        group.setPartitions(Arrays.asList(partition1, partition2));

        group.save(false);

        verify(partition1, never()).save();
        verify(partition2, never()).save();
    }

    @Test
    public void test_dehyrate(){

        String expectedTopic = "BaseGroupTest_Topic1";
        String expectedGroup = expectedTopic + "_Group1";
        String expectedDescription = "BaseGroupTest";
        String expectedPartition1Id = expectedGroup + "_Part1";
        String expectedPartition2Id = expectedGroup + "_Part2";

        TestPartition partition1 = createMockPartition(expectedPartition1Id);
        TestPartition partition2 = createMockPartition(expectedPartition2Id);

        TestProducerGroup group = new TestProducerGroup();

        group.setTopicId(expectedTopic);
        group.setGroupId(expectedGroup);
        group.setDescription(expectedDescription);
        group.setPartitions(Arrays.asList(partition1, partition2));

        BaseGroup.DehydratedState actualState = group.dehydrate();

        assertEquals(expectedTopic, actualState.getTopicId());
        assertEquals(expectedGroup, actualState.getGroupId());
        assertEquals(expectedDescription, actualState.getDescription());
        assertEquals(2, actualState.getPartitionIds().size());
        assertTrue(actualState.getPartitionIds().contains(expectedPartition1Id));
        assertTrue(actualState.getPartitionIds().contains(expectedPartition2Id));
        assertEquals(TestProducerGroup.class, actualState.getTopologyItemType());
        assertEquals(TestProducerGroup.EXTENSIONS, actualState.getExtensionProperties());
    }

    @Test
    public void test_restore(){

        String expectedTopic = "BaseGroupTest_Topic2";
        String expectedGroup = expectedTopic + "_Group2";
        String expectedDescription = "BaseGroupTest";
        String expectedPartition1Id = expectedGroup + "_Part1";
        String expectedPartition2Id = expectedGroup + "_Part2";
        HashMap<String, String> expectedExtensions = Maps.newHashMap();

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

        TestProducerGroup group = spy(new TestProducerGroup());

        group.restore(state);

        verify(group).set(expectedExtensions);

        assertEquals(expectedTopic, group.getTopicId());
        assertEquals(expectedGroup, group.getGroupId());
        assertEquals(expectedDescription, group.getDescription());
    }

    @Test
    public void test_getPartition() throws PartitionNotExistException {

        TestPartition expectedPartition1 = createMockPartition("p1");
        TestPartition expectedPartition2 = createMockPartition("p2");

        TestProducerGroup group = new TestProducerGroup();

        group.setPartitions(Arrays.asList(expectedPartition1, expectedPartition2));

        TestPartition actualPartition1 = group.getPartition("p1");
        TestPartition actualPartition2 = group.getPartition("p2");

        assertEquals(expectedPartition1, actualPartition1);
        assertEquals(expectedPartition2, actualPartition2);
    }

    @Test(expected = PartitionNotExistException.class)
    public void test_getPartition_exception_thrown_if_partition_does_not_exist() throws PartitionNotExistException {

        TestPartition partition = createMockPartition("p1");

        TestProducerGroup group = new TestProducerGroup();

        group.setPartitions(Arrays.asList(partition));

        group.getPartition("nonexistent");
    }

    @Test
    public void test_addPartition() throws Exception {

        LifeCycleListener.PartitionListener listener = mock(LifeCycleListener.PartitionListener.class);

        LifeCycleObserver.addListener(listener);

        TestPartition expectedPartition1 = createMockPartition("p1");
        TestPartition expectedPartition2 = createMockPartition("p2");

        TestProducerGroup group = new TestProducerGroup();

        group.addPartition(expectedPartition1);
        group.addPartition(expectedPartition2);

        Collection<TestPartition> partitions = group.getPartitions();

        assertEquals(2, partitions.size());
        assertTrue(partitions.contains(expectedPartition1));
        assertTrue(partitions.contains(expectedPartition2));

        // setPartitions should not call setup().
        verify(expectedPartition1).setup();
        verify(expectedPartition2).setup();

        verify(listener).onAdded(expectedPartition1);
        verify(listener).onAdded(expectedPartition2);
    }

    @Test(expected = PartitionAlreadyExistsException.class)
    public void test_addPartition_exception_thrown_if_partition_already_added() throws Exception {

        TestPartition expectedPartition = createMockPartition("p1");

        TestProducerGroup group = new TestProducerGroup();

        group.addPartition(expectedPartition);
        group.addPartition(expectedPartition);
    }

    @Test
    public void test_removePartition() throws Exception {

        LifeCycleListener.PartitionListener listener = mock(LifeCycleListener.PartitionListener.class);

        LifeCycleObserver.addListener(listener);

        TestPartition expectedPartition = createMockPartition("p1");

        TestProducerGroup group = new TestProducerGroup();

        group.setPartitions(Arrays.asList(expectedPartition));

        group.removePartition("p1");

        Collection<TestPartition> partitions = group.getPartitions();

        assertEquals(0, partitions.size());

        // setPartitions should not call setup().
        verify(expectedPartition).cleanup();

        verify(listener).onRemoved(expectedPartition);
    }

    @Test(expected = PartitionNotExistException.class)
    public void test_removePartition_exception_thrown_if_partition_not_exist() throws Exception {

        TestProducerGroup group = new TestProducerGroup();

        group.removePartition("p1");
    }

    @Test
    public void test_setPartitions() throws Exception {

        LifeCycleListener.PartitionListener listener = mock(LifeCycleListener.PartitionListener.class);

        LifeCycleObserver.addListener(listener);

        TestPartition expectedPartition1 = createMockPartition("p1");
        TestPartition expectedPartition2 = createMockPartition("p2");

        TestProducerGroup group = new TestProducerGroup();

        group.setPartitions(Arrays.asList(expectedPartition1, expectedPartition2));

        Collection<TestPartition> partitions = group.getPartitions();

        assertEquals(2, partitions.size());
        assertTrue(partitions.contains(expectedPartition1));
        assertTrue(partitions.contains(expectedPartition2));

        // setPartitions should not call setup().
        verify(expectedPartition1, never()).setup();
        verify(expectedPartition2, never()).setup();

        verifyZeroInteractions(listener);
    }


}
