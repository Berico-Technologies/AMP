package amp.topology.global.persistence;

import amp.topology.global.Group;
import amp.topology.global.PersistentTestBase;
import amp.topology.global.exceptions.TopologyGroupNotExistException;
import amp.topology.global.impl.BaseGroup;
import org.junit.Test;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class GroupPersistenceObserverTest extends PersistentTestBase {

    @Test
    public void test_onSaved(){

        BaseGroup group = mock(BaseGroup.class);

        BaseGroup.DehydratedState expectedState = mock(BaseGroup.DehydratedState.class);

        when(group.dehydrate()).thenReturn(expectedState);

        GroupPersistenceObserver observer = new GroupPersistenceObserver();

        observer.saveRequested(group);

        verify(PersistenceManager.groups()).save(expectedState);
    }

    @Test
    public void test_onRemoved() throws TopologyGroupNotExistException {

        String expectedGroup = "GroupPersistenceObserverTest";

        BaseGroup group = mock(BaseGroup.class);

        when(group.getGroupId()).thenReturn(expectedGroup);

        GroupPersistenceObserver observer = new GroupPersistenceObserver();

        observer.onRemoved(group);

        verify(PersistenceManager.groups()).remove(expectedGroup);
    }

    @Test
    public void test_getPersistable(){

        Group shouldBeBaseGroup = mock(BaseGroup.class);

        BaseGroup shouldNotBeNull = GroupPersistenceObserver.getPersistable(shouldBeBaseGroup);

        assertNotNull(shouldNotBeNull);

        Group shouldNotBeBaseGroup = mock(Group.class);

        BaseGroup shouldBeNull = GroupPersistenceObserver.getPersistable(shouldNotBeBaseGroup);

        assertNull(shouldBeNull);
    }
}
