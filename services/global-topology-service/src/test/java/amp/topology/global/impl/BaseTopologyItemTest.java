package amp.topology.global.impl;

import amp.topology.global.PersistentTestBase;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class BaseTopologyItemTest extends PersistentTestBase {

    @Test
    public void test_restore(){

        String expectedTopic = "BaseTopologyItemTest_Topic1";
        String expectedDescription = "BaseTopologyItemTest";

        TestTopologyItem topologyItem = spy(new TestTopologyItem());

        TopologyState state = new TopologyState(TestTopologyItem.class, expectedTopic, expectedDescription);

        topologyItem.restore(state);

        assertEquals(expectedTopic, topologyItem.getTopicId());
        assertEquals(expectedDescription, topologyItem.getDescription());

        verify(topologyItem).set(any(Map.class));
    }
}
