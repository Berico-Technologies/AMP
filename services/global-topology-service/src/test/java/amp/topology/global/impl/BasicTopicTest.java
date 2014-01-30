package amp.topology.global.impl;

import amp.topology.global.*;
import amp.topology.global.exceptions.ConnectorAlreadyExistsException;
import amp.topology.global.exceptions.ConnectorNotExistException;
import amp.topology.global.exceptions.TopologyGroupAlreadyExistsException;
import amp.topology.global.exceptions.TopologyGroupNotExistException;
import amp.topology.global.filtering.RouteFilterResults;
import amp.topology.global.filtering.RouteRequirements;
import amp.topology.global.lifecycle.LifeCycleListener;
import amp.topology.global.lifecycle.LifeCycleObserver;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class BasicTopicTest extends PersistentTestBase {

    @Test
    public void test_filter(){

        TestPartition expectedP1 = createMockPartition("p1");
        TestPartition expectedP2 = createMockPartition("p2");
        TestPartition expectedP3 = createMockPartition("p3");
        TestPartition expectedP4 = createMockPartition("p4");
        TestPartition expectedP5 = createMockPartition("p5");
        TestPartition expectedP6 = createMockPartition("p6");
        TestPartition expectedP7 = createMockPartition("p7");
        TestPartition expectedP8 = createMockPartition("p8");

        BaseProducerGroup pgroup1 = createMockProducerGroup("pg1");
        BaseProducerGroup pgroup2 = createMockProducerGroup("pg2");
        BaseConsumerGroup cgroup1 = createMockConsumerGroup("cg1");
        BaseConsumerGroup cgroup2 = createMockConsumerGroup("cg2");

        when(pgroup1.filter(any(RouteRequirements.class))).thenReturn(Arrays.asList(expectedP1, expectedP2));
        when(pgroup2.filter(any(RouteRequirements.class))).thenReturn(Arrays.asList(expectedP3, expectedP4));
        when(cgroup1.filter(any(RouteRequirements.class))).thenReturn(Arrays.asList(expectedP5, expectedP6));
        when(cgroup2.filter(any(RouteRequirements.class))).thenReturn(Arrays.asList(expectedP7, expectedP8));

        BasicTopic topic = new BasicTopic();

        topic.setProducerGroups(Arrays.<ProducerGroup<? extends Partition>>asList(pgroup1, pgroup2));

        topic.setConsumerGroups(Arrays.<ConsumerGroup<? extends Partition>>asList(cgroup1, cgroup2));

        RouteRequirements requirements = mock(RouteRequirements.class);

        RouteFilterResults results = topic.filter(requirements);

        assertEquals(4, results.getProducerPartitions().size());
        assertEquals(4, results.getConsumerPartitions().size());

        assertTrue(results.getProducerPartitions().contains(expectedP1));
        assertTrue(results.getProducerPartitions().contains(expectedP2));
        assertTrue(results.getProducerPartitions().contains(expectedP3));
        assertTrue(results.getProducerPartitions().contains(expectedP4));
        assertTrue(results.getConsumerPartitions().contains(expectedP5));
        assertTrue(results.getConsumerPartitions().contains(expectedP6));
        assertTrue(results.getConsumerPartitions().contains(expectedP7));
        assertTrue(results.getConsumerPartitions().contains(expectedP8));
    }

    @Test
    public void test_cleanup() throws Exception {

        String expectedTopic = "BaseGroupTest_Topic1";
        String expectedPGroup1 = expectedTopic + "_PGroup1";
        String expectedPGroup2 = expectedTopic + "_PGroup2";
        String expectedCGroup1 = expectedTopic + "_CGroup1";
        String expectedCGroup2 = expectedTopic + "_CGroup2";
        String expectedConnector1 = expectedTopic + "_Connector1";
        String expectedConnector2 = expectedTopic + "_Connector2";

        BaseProducerGroup pgroup1 = createMockProducerGroup(expectedPGroup1);
        BaseProducerGroup pgroup2 = createMockProducerGroup(expectedPGroup2);

        BaseConsumerGroup cgroup1 = createMockConsumerGroup(expectedCGroup1);
        BaseConsumerGroup cgroup2 = createMockConsumerGroup(expectedCGroup2);

        BaseConnector connector1 = createMockConnector(expectedConnector1);
        BaseConnector connector2 = createMockConnector(expectedConnector2);

        BasicTopic topic = new BasicTopic();

        topic.setProducerGroups(Arrays.<ProducerGroup<? extends Partition>>asList(pgroup1, pgroup2));
        topic.setConsumerGroups(Arrays.<ConsumerGroup<? extends Partition>>asList(cgroup1, cgroup2));
        topic.setConnectors(Arrays.<Connector<? extends Partition, ? extends Partition>>asList(connector1, connector2));

        topic.cleanup();

        verify(pgroup1).cleanup();
        verify(pgroup2).cleanup();
        verify(cgroup1).cleanup();
        verify(cgroup2).cleanup();
        verify(connector1).cleanup();
        verify(connector2).cleanup();

        assertEquals(0, topic.getProducerGroups().size());
        assertEquals(0, topic.getConsumerGroups().size());
        assertEquals(0, topic.getConnectors().size());
    }

    @Test
    public void test_save(){

        LifeCycleListener.TopicListener listener = mock(LifeCycleListener.TopicListener.class);

        LifeCycleObserver.addListener(listener);

        String expectedTopic = "BaseGroupTest_Topic1";
        String expectedDescription = "BaseGroupTest";
        String expectedPGroup1 = expectedTopic + "_PGroup1";
        String expectedPGroup2 = expectedTopic + "_PGroup2";
        String expectedCGroup1 = expectedTopic + "_CGroup1";
        String expectedCGroup2 = expectedTopic + "_CGroup2";
        String expectedConnector1 = expectedTopic + "_Connector1";
        String expectedConnector2 = expectedTopic + "_Connector2";

        BaseProducerGroup pgroup1 = createMockProducerGroup(expectedPGroup1);
        BaseProducerGroup pgroup2 = createMockProducerGroup(expectedPGroup2);

        BaseConsumerGroup cgroup1 = createMockConsumerGroup(expectedCGroup1);
        BaseConsumerGroup cgroup2 = createMockConsumerGroup(expectedCGroup2);

        BaseConnector connector1 = createMockConnector(expectedConnector1);
        BaseConnector connector2 = createMockConnector(expectedConnector2);

        BasicTopic topic = new BasicTopic();

        topic.setTopicId(expectedTopic);
        topic.setDescription(expectedDescription);
        topic.setProducerGroups(Arrays.<ProducerGroup<? extends Partition>>asList(pgroup1, pgroup2));
        topic.setConsumerGroups(Arrays.<ConsumerGroup<? extends Partition>>asList(cgroup1, cgroup2));
        topic.setConnectors(Arrays.<Connector<? extends Partition, ? extends Partition>>asList(connector1, connector2));

        topic.save();

        verify(pgroup1).save();
        verify(pgroup2).save();
        verify(cgroup1).save();
        verify(cgroup2).save();
        verify(connector1).save();
        verify(connector2).save();

        verify(listener).saveRequested(topic);
    }

    @Test
    public void test_save_does_not_save_aggregates(){

        String expectedTopic = "BaseGroupTest_Topic1";
        String expectedDescription = "BaseGroupTest";
        String expectedPGroup1 = expectedTopic + "_PGroup1";
        String expectedPGroup2 = expectedTopic + "_PGroup2";
        String expectedCGroup1 = expectedTopic + "_CGroup1";
        String expectedCGroup2 = expectedTopic + "_CGroup2";
        String expectedConnector1 = expectedTopic + "_Connector1";
        String expectedConnector2 = expectedTopic + "_Connector2";

        BaseProducerGroup pgroup1 = createMockProducerGroup(expectedPGroup1);
        BaseProducerGroup pgroup2 = createMockProducerGroup(expectedPGroup2);

        BaseConsumerGroup cgroup1 = createMockConsumerGroup(expectedCGroup1);
        BaseConsumerGroup cgroup2 = createMockConsumerGroup(expectedCGroup2);

        BaseConnector connector1 = createMockConnector(expectedConnector1);
        BaseConnector connector2 = createMockConnector(expectedConnector2);

        BasicTopic topic = new BasicTopic();

        topic.setTopicId(expectedTopic);
        topic.setDescription(expectedDescription);
        topic.setProducerGroups(Arrays.<ProducerGroup<? extends Partition>>asList(pgroup1, pgroup2));
        topic.setConsumerGroups(Arrays.<ConsumerGroup<? extends Partition>>asList(cgroup1, cgroup2));
        topic.setConnectors(Arrays.<Connector<? extends Partition, ? extends Partition>>asList(connector1, connector2));

        topic.save(false);

        verify(pgroup1, never()).save();
        verify(pgroup2, never()).save();
        verify(cgroup1, never()).save();
        verify(cgroup2, never()).save();
        verify(connector1, never()).save();
        verify(connector2, never()).save();
    }

    @Test
    public void test_dehydrate(){

        String expectedTopic = "BaseGroupTest_Topic1";
        String expectedDescription = "BaseGroupTest";
        String expectedPGroup1 = expectedTopic + "_PGroup1";
        String expectedPGroup2 = expectedTopic + "_PGroup2";
        String expectedCGroup1 = expectedTopic + "_CGroup1";
        String expectedCGroup2 = expectedTopic + "_CGroup2";
        String expectedConnector1 = expectedTopic + "_Connector1";
        String expectedConnector2 = expectedTopic + "_Connector2";

        BaseProducerGroup pgroup1 = createMockProducerGroup(expectedPGroup1);
        BaseProducerGroup pgroup2 = createMockProducerGroup(expectedPGroup2);

        BaseConsumerGroup cgroup1 = createMockConsumerGroup(expectedCGroup1);
        BaseConsumerGroup cgroup2 = createMockConsumerGroup(expectedCGroup2);

        BaseConnector connector1 = createMockConnector(expectedConnector1);
        BaseConnector connector2 = createMockConnector(expectedConnector2);

        BasicTopic topic = new BasicTopic();

        topic.setTopicId(expectedTopic);
        topic.setDescription(expectedDescription);
        topic.setProducerGroups(Arrays.<ProducerGroup<? extends Partition>>asList(pgroup1, pgroup2));
        topic.setConsumerGroups(Arrays.<ConsumerGroup<? extends Partition>>asList(cgroup1, cgroup2));
        topic.setConnectors(Arrays.<Connector<? extends Partition, ? extends Partition>>asList(connector1, connector2));

        BasicTopic.DehydratedState actualState = topic.dehydrate();

        assertEquals(expectedTopic, actualState.getTopicId());
        assertEquals(expectedDescription, actualState.getDescription());
        assertEquals(2, actualState.getProducerGroupIds().size());
        assertEquals(2, actualState.getConsumerGroupIds().size());
        assertEquals(2, actualState.getConnectorIds().size());
        assertTrue(actualState.getProducerGroupIds().contains(expectedPGroup1));
        assertTrue(actualState.getProducerGroupIds().contains(expectedPGroup2));
        assertTrue(actualState.getConsumerGroupIds().contains(expectedCGroup1));
        assertTrue(actualState.getConsumerGroupIds().contains(expectedCGroup2));
        assertTrue(actualState.getConnectorIds().contains(expectedConnector1));
        assertTrue(actualState.getConnectorIds().contains(expectedConnector2));
        assertEquals(BasicTopic.class, actualState.getTopologyItemType());
    }

    @Test
    public void test_groupExists(){

        BaseProducerGroup expectedPGroup = createMockProducerGroup("pgroup");
        BaseConsumerGroup expectedCGroup = createMockConsumerGroup("cgroup");

        BasicTopic topic = new BasicTopic();

        topic.setProducerGroups(Arrays.<ProducerGroup<? extends Partition>>asList(expectedPGroup));
        topic.setConsumerGroups(Arrays.<ConsumerGroup<? extends Partition>>asList(expectedCGroup));

        assertTrue(topic.groupExists("pgroup"));
        assertTrue(topic.groupExists("cgroup"));
        assertFalse(topic.groupExists("nonexistent"));
    }

    @Test
    public void test_getGroupExists(){

        BaseProducerGroup expectedPGroup = createMockProducerGroup("pgroup");
        BaseConsumerGroup expectedCGroup = createMockConsumerGroup("cgroup");

        BasicTopic topic = new BasicTopic();

        topic.setProducerGroups(Arrays.<ProducerGroup<? extends Partition>>asList(expectedPGroup));
        topic.setConsumerGroups(Arrays.<ConsumerGroup<? extends Partition>>asList(expectedCGroup));

        assertEquals(Topic.GroupExists.AsProducer, topic.getGroupExists("pgroup"));
        assertEquals(Topic.GroupExists.AsConsumer, topic.getGroupExists("cgroup"));
        assertEquals(Topic.GroupExists.False, topic.getGroupExists("nonexistent"));
    }

    @Test
    public void test_getGroup() throws Exception {

        BaseProducerGroup expectedPGroup = createMockProducerGroup("pgroup");
        BaseConsumerGroup expectedCGroup = createMockConsumerGroup("cgroup");

        BasicTopic topic = new BasicTopic();

        topic.setProducerGroups(Arrays.<ProducerGroup<? extends Partition>>asList(expectedPGroup));
        topic.setConsumerGroups(Arrays.<ConsumerGroup<? extends Partition>>asList(expectedCGroup));

        Group<? extends Partition> actualPGroup = topic.getGroup("pgroup");
        Group<? extends Partition> actualCGroup = topic.getGroup("cgroup");

        assertEquals(expectedPGroup, actualPGroup);
        assertEquals(expectedCGroup, actualCGroup);
    }

    @Test(expected = TopologyGroupNotExistException.class)
    public void test_getGroup_exception_thrown_if_group_not_exist() throws Exception {

        BaseProducerGroup expectedPGroup = createMockProducerGroup("pgroup");
        BaseConsumerGroup expectedCGroup = createMockConsumerGroup("cgroup");

        BasicTopic topic = new BasicTopic();

        topic.setProducerGroups(Arrays.<ProducerGroup<? extends Partition>>asList(expectedPGroup));
        topic.setConsumerGroups(Arrays.<ConsumerGroup<? extends Partition>>asList(expectedCGroup));

        topic.getGroup("nonexistent");
    }

    @Test
    public void test_removeGroup() throws Exception {

        LifeCycleListener.GroupListener listener = mock(LifeCycleListener.GroupListener.class);

        LifeCycleObserver.addListener(listener);

        BaseProducerGroup expectedPGroup = createMockProducerGroup("pgroup");
        BaseConsumerGroup expectedCGroup = createMockConsumerGroup("cgroup");

        BasicTopic topic = new BasicTopic();

        topic.setProducerGroups(Arrays.<ProducerGroup<? extends Partition>>asList(expectedPGroup));
        topic.setConsumerGroups(Arrays.<ConsumerGroup<? extends Partition>>asList(expectedCGroup));

        topic.removeGroup("pgroup");
        topic.removeGroup("cgroup");

        verify(expectedPGroup).cleanup();
        verify(expectedCGroup).cleanup();

        verify(listener).onRemoved(expectedPGroup);
        verify(listener).onRemoved(expectedCGroup);
    }

    @Test(expected = TopologyGroupNotExistException.class)
    public void test_removeGroup_exception_thrown_if_group_not_exist() throws Exception {

        BaseProducerGroup expectedPGroup = createMockProducerGroup("pgroup");
        BaseConsumerGroup expectedCGroup = createMockConsumerGroup("cgroup");

        BasicTopic topic = new BasicTopic();

        topic.setProducerGroups(Arrays.<ProducerGroup<? extends Partition>>asList(expectedPGroup));
        topic.setConsumerGroups(Arrays.<ConsumerGroup<? extends Partition>>asList(expectedCGroup));

        topic.removeGroup("nonexistent");
    }

    @Test
    public void test_addGroup() throws Exception {

        LifeCycleListener.GroupListener listener = mock(LifeCycleListener.GroupListener.class);

        LifeCycleObserver.addListener(listener);

        BaseProducerGroup expectedPGroup = createMockProducerGroup("pgroup");
        BaseConsumerGroup expectedCGroup = createMockConsumerGroup("cgroup");

        BasicTopic topic = new BasicTopic();

        topic.addGroup(expectedPGroup);
        topic.addGroup(expectedCGroup);

        verify(expectedPGroup).setup();
        verify(expectedCGroup).setup();

        verify(listener).onAdded(expectedPGroup);
        verify(listener).onAdded(expectedCGroup);
    }

    @Test(expected = TopologyGroupAlreadyExistsException.class)
    public void test_addGroup_exception_thrown_if_pgroup_already_exists() throws Exception {

        BaseProducerGroup expectedPGroup = createMockProducerGroup("pgroup");

        BasicTopic topic = new BasicTopic();

        topic.addGroup(expectedPGroup);

        topic.addGroup(expectedPGroup);
    }

    @Test(expected = TopologyGroupAlreadyExistsException.class)
    public void test_addGroup_exception_thrown_if_cgroup_already_exists() throws Exception {

        BaseConsumerGroup expectedCGroup = createMockConsumerGroup("cgroup");

        BasicTopic topic = new BasicTopic();

        topic.addGroup(expectedCGroup);

        topic.addGroup(expectedCGroup);
    }

    @Test
    public void test_getProducerGroup() throws TopologyGroupNotExistException {

        BaseProducerGroup expectedGroup1 = createMockProducerGroup("pg1");
        BaseProducerGroup expectedGroup2 = createMockProducerGroup("pg2");

        BasicTopic topic = new BasicTopic();

        topic.setProducerGroups(Arrays.<ProducerGroup<? extends Partition>>asList(expectedGroup1, expectedGroup2));

        ProducerGroup<? extends Partition> actualGroup1 = topic.getProducerGroup("pg1");
        ProducerGroup<? extends Partition> actualGroup2 = topic.getProducerGroup("pg2");

        assertEquals(expectedGroup1, actualGroup1);
        assertEquals(expectedGroup2, actualGroup2);
    }

    @Test(expected = TopologyGroupNotExistException.class)
    public void test_getProducerGroup_exception_thrown_if_group_not_exist() throws TopologyGroupNotExistException {

        BaseProducerGroup group = createMockProducerGroup("pg1");

        BasicTopic topic = new BasicTopic();

        topic.setProducerGroups(Arrays.<ProducerGroup<? extends Partition>>asList(group));

        topic.getProducerGroup("nonexistent");
    }

    @Test
    public void test_getConsumerGroup() throws TopologyGroupNotExistException {

        BaseConsumerGroup expectedGroup1 = createMockConsumerGroup("cg1");
        BaseConsumerGroup expectedGroup2 = createMockConsumerGroup("cg2");

        BasicTopic topic = new BasicTopic();

        topic.setConsumerGroups(Arrays.<ConsumerGroup<? extends Partition>>asList(expectedGroup1, expectedGroup2));

        ConsumerGroup<? extends Partition> actualGroup1 = topic.getConsumerGroup("cg1");
        ConsumerGroup<? extends Partition> actualGroup2 = topic.getConsumerGroup("cg2");

        assertEquals(expectedGroup1, actualGroup1);
        assertEquals(expectedGroup2, actualGroup2);
    }

    @Test(expected = TopologyGroupNotExistException.class)
    public void test_getConsumerGroup_exception_thrown_if_group_not_exist() throws TopologyGroupNotExistException {

        BaseConsumerGroup group = createMockConsumerGroup("cg1");

        BasicTopic topic = new BasicTopic();

        topic.setConsumerGroups(Arrays.<ConsumerGroup<? extends Partition>>asList(group));

        topic.getConsumerGroup("nonexistent");
    }

    @Test
    public void test_setProducerGroups() throws Exception {

        BaseProducerGroup expectedGroup1 = createMockProducerGroup("pg1");
        BaseProducerGroup expectedGroup2 = createMockProducerGroup("pg2");

        BasicTopic topic = new BasicTopic();

        topic.setProducerGroups(Arrays.<ProducerGroup<? extends Partition>>asList(expectedGroup1, expectedGroup2));

        Collection<ProducerGroup<? extends Partition>> actualGroups = topic.getProducerGroups();

        assertEquals(2, actualGroups.size());
        assertTrue(actualGroups.contains(expectedGroup1));
        assertTrue(actualGroups.contains(expectedGroup2));

        verify(expectedGroup1, never()).setup();
        verify(expectedGroup2, never()).setup();
    }

    @Test
    public void test_setConsumerGroups() throws Exception {

        BaseConsumerGroup expectedGroup1 = createMockConsumerGroup("cg1");
        BaseConsumerGroup expectedGroup2 = createMockConsumerGroup("cg2");

        BasicTopic topic = new BasicTopic();

        topic.setConsumerGroups(Arrays.<ConsumerGroup<? extends Partition>>asList(expectedGroup1, expectedGroup2));

        Collection<ConsumerGroup<? extends Partition>> actualGroups = topic.getConsumerGroups();

        assertEquals(2, actualGroups.size());
        assertTrue(actualGroups.contains(expectedGroup1));
        assertTrue(actualGroups.contains(expectedGroup2));

        verify(expectedGroup1, never()).setup();
        verify(expectedGroup2, never()).setup();
    }

    @Test
    public void test_addProducerGroup() throws Exception {

        LifeCycleListener.GroupListener listener = mock(LifeCycleListener.GroupListener.class);

        LifeCycleObserver.addListener(listener);

        BaseProducerGroup group = createMockProducerGroup("pg1");

        BasicTopic topic = new BasicTopic();

        topic.addProducerGroup(group);

        verify(group).setup();

        verify(listener).onAdded(group);
    }

    @Test(expected = TopologyGroupAlreadyExistsException.class)
    public void test_addProducerGroup_exception_thrown_if_group_already_exists() throws Exception {

        BaseProducerGroup group = createMockProducerGroup("pg1");

        BasicTopic topic = new BasicTopic();

        topic.addProducerGroup(group);

        topic.addProducerGroup(group);
    }

    @Test
    public void test_addConsumerGroup() throws Exception {

        LifeCycleListener.GroupListener listener = mock(LifeCycleListener.GroupListener.class);

        LifeCycleObserver.addListener(listener);

        BaseConsumerGroup group = createMockConsumerGroup("cg1");

        BasicTopic topic = new BasicTopic();

        topic.addConsumerGroup(group);

        verify(group).setup();

        verify(listener).onAdded(group);
    }

    @Test(expected = TopologyGroupAlreadyExistsException.class)
    public void test_addConsumerGroup_exception_thrown_if_group_already_exists() throws Exception {

        BaseConsumerGroup group = createMockConsumerGroup("cg1");

        BasicTopic topic = new BasicTopic();

        topic.addConsumerGroup(group);

        topic.addConsumerGroup(group);
    }

    @Test
    public void test_removeProducerGroup() throws Exception {

        LifeCycleListener.GroupListener listener = mock(LifeCycleListener.GroupListener.class);

        LifeCycleObserver.addListener(listener);

        BaseProducerGroup group = createMockProducerGroup("pg1");

        BasicTopic topic = new BasicTopic();

        topic.setProducerGroups(Arrays.<ProducerGroup<? extends Partition>>asList(group));

        topic.removeProducerGroup("pg1");

        verify(group).cleanup();

        verify(listener).onRemoved(group);
    }

    @Test(expected = TopologyGroupNotExistException.class)
    public void test_removeProducerGroup_exception_thrown_if_group_not_exist() throws Exception {

        BaseProducerGroup group = createMockProducerGroup("pg1");

        BasicTopic topic = new BasicTopic();

        topic.setProducerGroups(Arrays.<ProducerGroup<? extends Partition>>asList(group));

        topic.removeProducerGroup("nonexistent");
    }

    @Test
    public void test_removeConsumerGroup() throws Exception {

        LifeCycleListener.GroupListener listener = mock(LifeCycleListener.GroupListener.class);

        LifeCycleObserver.addListener(listener);

        BaseConsumerGroup group = createMockConsumerGroup("cg1");

        BasicTopic topic = new BasicTopic();

        topic.setConsumerGroups(Arrays.<ConsumerGroup<? extends Partition>>asList(group));

        topic.removeConsumerGroup("cg1");

        verify(group).cleanup();

        verify(listener).onRemoved(group);
    }

    @Test(expected = TopologyGroupNotExistException.class)
    public void test_removeConsumerGroup_exception_thrown_if_group_not_exist() throws Exception {

        BaseConsumerGroup group = createMockConsumerGroup("cg1");

        BasicTopic topic = new BasicTopic();

        topic.setConsumerGroups(Arrays.<ConsumerGroup<? extends Partition>>asList(group));

        topic.removeConsumerGroup("nonexistent");
    }

    @Test
    public void test_addConnector() throws Exception {

        LifeCycleListener.ConnectorListener listener = mock(LifeCycleListener.ConnectorListener.class);

        LifeCycleObserver.addListener(listener);

        BaseConnector connector = createMockConnector("c1");

        BasicTopic topic = new BasicTopic();

        topic.addConnector(connector);

        verify(connector).setup();

        verify(listener).onAdded(connector);
    }

    @Test(expected = ConnectorAlreadyExistsException.class)
    public void test_addConnector_exception_thrown_if_connector_already_exists() throws Exception {

        BaseConnector connector = createMockConnector("c1");

        BasicTopic topic = new BasicTopic();

        topic.addConnector(connector);

        topic.addConnector(connector);
    }

    @Test
    public void test_removeConnector() throws Exception {

        LifeCycleListener.ConnectorListener listener = mock(LifeCycleListener.ConnectorListener.class);

        LifeCycleObserver.addListener(listener);

        BaseConnector connector = createMockConnector("c1");

        BasicTopic topic = new BasicTopic();

        topic.setConnectors(
                Arrays.<Connector<? extends Partition, ? extends Partition>>asList(connector));

        topic.removeConnector("c1");

        verify(connector).cleanup();

        verify(listener).onRemoved(connector);
    }

    @Test(expected = ConnectorNotExistException.class)
    public void test_removeConnector_exception_thrown_if_connector_not_exists() throws Exception {

        BaseConnector connector = createMockConnector("c1");

        BasicTopic topic = new BasicTopic();

        topic.setConnectors(
                Arrays.<Connector<? extends Partition, ? extends Partition>>asList(connector));

        topic.removeConnector("nonexistent");
    }

    @Test
    public void test_getConnector() throws ConnectorNotExistException {

        BaseConnector expectedConnector1 = createMockConnector("c1");
        BaseConnector expectedConnector2 = createMockConnector("c2");

        BasicTopic topic = new BasicTopic();

        topic.setConnectors(
                Arrays.<Connector<? extends Partition, ? extends Partition>>asList(
                        expectedConnector1, expectedConnector2));

        Connector<? extends Partition, ? extends Partition> actualConnector1 = topic.getConnector("c1");
        Connector<? extends Partition, ? extends Partition> actualConnector2 = topic.getConnector("c2");

        assertEquals(expectedConnector1, actualConnector1);
        assertEquals(expectedConnector2, actualConnector2);
    }

    @Test(expected = ConnectorNotExistException.class)
    public void test_getConnector_exception_thrown_if_connector_not_exists() throws ConnectorNotExistException {

        BaseConnector connector = createMockConnector("c1");

        BasicTopic topic = new BasicTopic();

        topic.setConnectors(
                Arrays.<Connector<? extends Partition, ? extends Partition>>asList(connector));

        topic.getConnector("nonexistent");
    }

    @Test
    public void test_setConnectors() throws Exception {

        BaseConnector expectedConnector1 = createMockConnector("c1");
        BaseConnector expectedConnector2 = createMockConnector("c2");

        BasicTopic basicTopic = new BasicTopic();

        basicTopic.setConnectors(
                Arrays.<Connector<? extends Partition, ? extends Partition>>asList(
                        expectedConnector1, expectedConnector2));

        Collection<Connector<? extends Partition, ? extends Partition>> connectors = basicTopic.getConnectors();

        assertEquals(2, connectors.size());
        assertTrue(connectors.contains(expectedConnector1));
        assertTrue(connectors.contains(expectedConnector2));

        verify(expectedConnector1, never()).setup();
        verify(expectedConnector2, never()).setup();
    }

    @Test
    public void test_static_cleanup() throws Exception {

        BaseTopologyItem shouldBeCleaned = mock(BaseTopologyItem.class);

        BasicTopic.cleanup(shouldBeCleaned);

        verify(shouldBeCleaned).cleanup();

        Partition shouldNotBeCleaned = mock(Partition.class);

        BasicTopic.cleanup(shouldNotBeCleaned);

        verifyZeroInteractions(shouldNotBeCleaned);
    }

    @Test
    public void test_state_setup() throws Exception {

        BaseTopologyItem shouldBeSetup = mock(BaseTopologyItem.class);

        BasicTopic.setup(shouldBeSetup);

        verify(shouldBeSetup).setup();

        Partition shouldNotBeSetup = mock(Partition.class);

        BasicTopic.setup(shouldNotBeSetup);

        verifyZeroInteractions(shouldNotBeSetup);
    }


}
