package amp.topology.snapshot;

import amp.topology.global.TopicRegistry;
import com.yammer.metrics.core.HealthCheck;
import org.joda.time.DateTime;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class SnapshotHealthCheckTest {


    @Test
     public void test_check__returns_unhealthy_if_there_are_no_snapshots() throws Exception {

        SnapshotManager manager = createMockSnapshotManager(-1);

        TopicRegistry registry = createMockTopicRegistry(-1);

        SnapshotHealthCheck healthCheck = new SnapshotHealthCheck(manager, registry);

        HealthCheck.Result result = healthCheck.check();

        assertFalse(result.isHealthy());
    }

    @Test
    public void test_check__returns_unhealthy_if_the_stale_threshold_is_reached() throws Exception {

        SnapshotManager manager = createMockSnapshotManager(
                DateTime
                        .now()
                        .minus(SnapshotHealthCheck.UNHEALTH_STALE_SNAPSHOT_TIME + 10)
                        .getMillis());

        TopicRegistry registry = createMockTopicRegistry(DateTime.now().getMillis());

        SnapshotHealthCheck healthCheck = new SnapshotHealthCheck(manager, registry);

        HealthCheck.Result result = healthCheck.check();

        assertFalse(result.isHealthy());
    }

    @Test
    public void test_check__returns_unhealthy_if_snapshot_is_earlier_than_last_modified_but_not_stale() throws Exception {

        TopicRegistry registry = createMockTopicRegistry(DateTime.now().getMillis());

        SnapshotManager manager = createMockSnapshotManager(
                DateTime
                        .now()
                        .minus(SnapshotHealthCheck.UNHEALTH_STALE_SNAPSHOT_TIME - 60000)
                        .getMillis());

        SnapshotHealthCheck healthCheck = new SnapshotHealthCheck(manager, registry);

        HealthCheck.Result result = healthCheck.check();

        assertTrue(result.isHealthy());
    }

    @Test
    public void test_check__returns_healthy_if_snapshot_is_later_than_last_modified() throws Exception {

        TopicRegistry registry = createMockTopicRegistry(DateTime.now().minus(100000).getMillis());

        SnapshotManager manager = createMockSnapshotManager(
                DateTime
                        .now()
                        .getMillis());

        SnapshotHealthCheck healthCheck = new SnapshotHealthCheck(manager, registry);

        HealthCheck.Result result = healthCheck.check();

        assertTrue(result.isHealthy());
    }

    protected static SnapshotManager createMockSnapshotManager(long lastPersisted){

        SnapshotManager manager = mock(SnapshotManager.class);

        when(manager.lastPersisted()).thenReturn(lastPersisted);

        return manager;
    }

    protected static TopicRegistry createMockTopicRegistry(long lastModified){

        TopicRegistry registry = mock(TopicRegistry.class);

        when(registry.lastModified()).thenReturn(lastModified);

        return registry;
    }
}
