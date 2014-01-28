package amp.topology.global.impl;

import amp.topology.global.Connector;
import amp.topology.global.ConsumerGroup;
import amp.topology.global.ProducerGroup;
import amp.topology.global.lifecycle.LifeCycleListener;
import amp.topology.global.lifecycle.LifeCycleObserver;
import amp.topology.global.persistence.PersistenceManager;
import com.google.common.collect.Maps;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class BaseConnectorTest extends PersistentTestBase {

    @Test
    public void test_save(){

        String expectedTopic = "BaseConnectorTest_Topic1";
        String expectedConnector = expectedTopic + "_Group1";
        String expectedDescription = "BaseConnectorTest";
        String expectedPGroup = expectedTopic + "_PGroup";
        String expectedCGroup = expectedTopic + "_CGroup";

        ProducerGroup<TestPartition> pgroup = mock(ProducerGroup.class);

        when(pgroup.getGroupId()).thenReturn(expectedPGroup);

        ConsumerGroup<TestPartition> cgroup = mock(ConsumerGroup.class);

        when(cgroup.getGroupId()).thenReturn(expectedCGroup);

        TestConnector connector = new TestConnector();

        connector.setTopicId(expectedTopic);
        connector.setDescription(expectedDescription);
        connector.setConnectorId(expectedConnector);
        connector.setProducerGroup(pgroup);
        connector.setConsumerGroup(cgroup);

        connector.save();

        ArgumentCaptor<BaseConnector.DehydratedState> captor =
                ArgumentCaptor.forClass(BaseConnector.DehydratedState.class);

        verify(PersistenceManager.connectors()).save(captor.capture());

        BaseConnector.DehydratedState actualState = locateConnectorState(captor, expectedConnector);

        assertEquals(expectedTopic, actualState.getTopicId());
        assertEquals(expectedConnector, actualState.getConnectorId());
        assertEquals(expectedDescription, actualState.getDescription());
        assertEquals(expectedPGroup, actualState.getProducerGroupId());
        assertEquals(expectedCGroup, actualState.getConsumerGroupId());
        assertEquals(TestConnector.class, actualState.getTopologyItemType());
        assertEquals(TestConnector.EXTENSIONS, actualState.getExtensionProperties());
    }

    @Test
    public void test_restore() throws Exception {

        // Note:  The responsibility for the Hydration of Groups exists outside
        // of Connector.  therefore, are "restore" on Connector will not initialize the
        // Groups.

        String expectedTopic = "BaseConnectorTest_Topic2";
        String expectedConnector = expectedTopic + "_Group2";
        String expectedDescription = "BaseConnectorTest";
        HashMap<String, String> expectedExtensions = Maps.newHashMap();

        BaseConnector.DehydratedState state = new BaseConnector.DehydratedState(
                TestConnector.class, expectedTopic, expectedConnector, expectedDescription, null, null, expectedExtensions);

        TestConnector connector = spy(new TestConnector());

        connector.restore(state);

        verify(connector).set(expectedExtensions);

        assertEquals(expectedTopic, connector.getTopicId());
        assertEquals(expectedConnector, connector.getConnectorId());
        assertEquals(expectedDescription, connector.getDescription());
    }

    @Test
    public void test_setState(){

        LifeCycleListener.ConnectorListener listener = mock(LifeCycleListener.ConnectorListener.class);

        LifeCycleObserver.addListener(listener);

        TestConnector testConnector = new TestConnector();

        String expectedReason = "Cause I say so!";

        testConnector.changeState(Connector.ConnectorStates.INACTIVE, expectedReason);

        verify(listener).onStateChange(
                testConnector,
                Connector.ConnectorStates.NONEXISTENT,
                Connector.ConnectorStates.INACTIVE,
                expectedReason);
    }
}