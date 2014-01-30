package amp.topology.global.impl;

import amp.topology.global.Connector;
import amp.topology.global.ConsumerGroup;
import amp.topology.global.PersistentTestBase;
import amp.topology.global.ProducerGroup;
import amp.topology.global.lifecycle.LifeCycleListener;
import amp.topology.global.lifecycle.LifeCycleObserver;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class BaseConnectorTest extends PersistentTestBase {

    @Test
     public void test_save(){

        LifeCycleListener.ConnectorListener listener = mock(LifeCycleListener.ConnectorListener.class);

        LifeCycleObserver.addListener(listener);

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

        verify(pgroup).save();
        verify(cgroup).save();

        verify(listener).saveRequested(connector);
    }

    @Test
    public void test_save_does_not_save_aggregates(){

        String expectedTopic = "BaseConnectorTest_Topic4";
        String expectedConnector = expectedTopic + "_Group4";
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

        connector.save(false);

        verify(pgroup, never()).save();
        verify(cgroup, never()).save();
    }

    @Test
    public void test_dehydrate(){

        String expectedTopic = "BaseConnectorTest_Topic3";
        String expectedConnector = expectedTopic + "_Group3";
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

        BaseConnector.DehydratedState actualState = connector.dehydrate();

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
