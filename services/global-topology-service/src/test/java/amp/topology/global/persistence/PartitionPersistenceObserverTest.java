package amp.topology.global.persistence;

import amp.topology.global.Partition;
import amp.topology.global.PersistentTestBase;
import amp.topology.global.exceptions.PartitionNotExistException;
import amp.topology.global.impl.BasePartition;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class PartitionPersistenceObserverTest extends PersistentTestBase {

    @Test
    public void test_onSaved(){

        BasePartition partition = mock(BasePartition.class);

        BasePartition.DehydratedState expectedState = mock(BasePartition.DehydratedState.class);

        when(partition.dehydrate()).thenReturn(expectedState);

        PartitionPersistenceObserver observer = new PartitionPersistenceObserver();

        observer.saveRequested(partition);

        verify(PersistenceManager.partitions()).save(expectedState);
    }

    @Test
    public void test_onRemoved() throws PartitionNotExistException {

        String expectedPartition = "PartitionPersistenceObserverTest";

        BasePartition partition = mock(BasePartition.class);

        when(partition.getPartitionId()).thenReturn(expectedPartition);

        PartitionPersistenceObserver observer = new PartitionPersistenceObserver();

        observer.onRemoved(partition);

        verify(PersistenceManager.partitions()).remove(expectedPartition);
    }

    @Test
    public void test_getPersistable(){

        Partition shouldBeBasePartition = mock(BasePartition.class);

        BasePartition shouldNotBeNull = PartitionPersistenceObserver.getPersistable(shouldBeBasePartition);

        assertNotNull(shouldNotBeNull);

        Partition shouldNotBeBasePartition = mock(Partition.class);

        BasePartition shouldBeNull = PartitionPersistenceObserver.getPersistable(shouldNotBeBasePartition);

        assertNull(shouldBeNull);
    }
}
