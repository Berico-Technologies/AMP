package amp.topology.snapshot;

import amp.topology.global.Topic;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * A Topology snapshot record.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class Snapshot {

    protected String id;

    protected @Nullable String description;

    protected long timestamp;

    protected Collection<Topic> topics;

    /**
     * Please do not use.  This is for serialization purposes only.
     */
    public Snapshot(){}

    public Snapshot(String id, @Nullable String description, long timestamp, Collection<Topic> topics) {
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

    public Collection<Topic> getTopics() {
        return topics;
    }
}
