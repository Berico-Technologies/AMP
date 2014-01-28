package amp.topology.snapshot;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * A Topology snapshot record.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class Snapshot {

    protected String id;

    protected String description;

    protected long timestamp;

    protected Collection<amp.topology.global.Topic> topics;

    /**
     * Please do not use.  This is for serialization purposes only.
     */
    public Snapshot(){}

    public Snapshot(String id, @Nullable String description, long timestamp, Collection<amp.topology.global.Topic> topics) {
        this.id = id;
        this.description = description;
        this.timestamp = timestamp;
        this.topics = topics;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Collection<amp.topology.global.Topic> getTopics() {
        return topics;
    }
}
