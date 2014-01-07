package amp.topology.snapshot;

import amp.topology.global.TopicRegistry;
import com.yammer.metrics.core.HealthCheck;

/**
 * Check to see if the latest snapshot is beyond a reasonable "staleness" threshold.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class SnapshotHealthCheck extends HealthCheck {

    public static long UNHEALTH_STALE_SNAPSHOT_TIME = 30 * 60 * 1000;

    SnapshotManager snapshotManager;

    TopicRegistry topicRegistry;

    public SnapshotHealthCheck(SnapshotManager snapshotManager, TopicRegistry topicRegistry) {
        super("snapshot");
        this.snapshotManager = snapshotManager;
        this.topicRegistry = topicRegistry;
    }

    @Override
    protected Result check() throws Exception {

        long lastTimePersisted = snapshotManager.lastPersisted();

        long lastModified = topicRegistry.lastModified();

        if (lastTimePersisted == -1)
            return Result.unhealthy("No 'latest' topology snapshot; LastMod: %s", lastModified);

        long staleTime = lastModified - lastTimePersisted;

        if (staleTime > UNHEALTH_STALE_SNAPSHOT_TIME)
            return Result.unhealthy("Beyond stale threshold: LastMod: %s, LastPersisted: %s, StaleTime: %s",
                    lastModified, lastTimePersisted, staleTime);

        return Result.healthy("OK: LastMod: %s, LastPersisted: %s", lastModified, lastTimePersisted);
    }
}
