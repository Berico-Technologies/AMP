package amp.topology.global.impl;

import amp.topology.global.Partition;
import amp.topology.global.PersistentTestBase;
import amp.topology.global.lifecycle.LifeCycleListener;
import amp.topology.global.lifecycle.LifeCycleObserver;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class BasePartitionTest extends PersistentTestBase {

    @Test
    public void test_save(){

        LifeCycleListener.PartitionListener listener = mock(LifeCycleListener.PartitionListener.class);

        LifeCycleObserver.addListener(listener);

        String expectedTopic = "BasePartitionTest_Topic1";
        String expectedGroup = expectedTopic + "_Group1";
        String expectedPartition = expectedGroup + "_Partition1";
        String expectedDescription = "BasePartitionTest";

        TestPartition partition = new TestPartition();

        partition.setTopicId(expectedTopic);
        partition.setGroupId(expectedGroup);
        partition.setPartitionId(expectedPartition);
        partition.setDescription(expectedDescription);

        partition.save();

        verify(listener).saveRequested(partition);
    }

    @Test
    public void test_dehydrate(){

        String expectedTopic = "BasePartitionTest_Topic3";
        String expectedGroup = expectedTopic + "_Group3";
        String expectedPartition = expectedGroup + "_Partition3";
        String expectedDescription = "BasePartitionTest";

        TestPartition partition = new TestPartition();

        partition.setTopicId(expectedTopic);
        partition.setGroupId(expectedGroup);
        partition.setPartitionId(expectedPartition);
        partition.setDescription(expectedDescription);

        BasePartition.DehydratedState actualState = partition.dehydrate();

        assertEquals(expectedTopic, actualState.getTopicId());
        assertEquals(expectedGroup, actualState.getGroupId());
        assertEquals(expectedPartition, actualState.getPartitionId());
        assertEquals(expectedDescription, actualState.getDescription());
        assertEquals(TestPartition.class, actualState.getTopologyItemType());
        assertEquals(TestPartition.EXTENSIONS, actualState.getExtensionProperties());
    }

    @Test
    public void test_restore(){

        TestPartition partition = new TestPartition();

        String expectedTopic = "BasePartitionTest_Topic2";
        String expectedGroup = expectedTopic + "_Group2";
        String expectedPartition = expectedGroup + "_Partition2";
        String expectedDescription = "BasePartitionTest";

        BasePartition.DehydratedState state =
                new BasePartition.DehydratedState(TestPartition.class, expectedTopic, expectedDescription, expectedGroup, expectedPartition);

        partition.restore(state);

        assertEquals(expectedTopic, partition.getTopicId());
        assertEquals(expectedGroup, partition.getGroupId());
        assertEquals(expectedPartition, partition.getPartitionId());
        assertEquals(expectedDescription, partition.getDescription());
    }

    @Test
    public void test_setState(){

        LifeCycleListener.PartitionListener listener = mock(LifeCycleListener.PartitionListener.class);

        LifeCycleObserver.addListener(listener);

        TestPartition partition = new TestPartition();

        String expectedReason =  "Here is my reason.";

        partition.changeState(Partition.PartitionStates.INACTIVE, expectedReason);

        verify(listener)
            .onStateChange(
                    partition,
                    Partition.PartitionStates.NONEXISTENT,
                    Partition.PartitionStates.INACTIVE,
                    expectedReason);
    }
}
