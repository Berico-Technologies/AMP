package amp.topology.global;

import java.util.Map;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public interface TopologyItem {

    String getTopicId();

    void setTopicId(String id);

    String getDescription();

    void setDescription(String description);

    /**
     * Save the entity and it's aggregates.
     */
    void save();

    /**
     * Save the entity, optionally saving aggregates. (Default should be true).
     */
    void save(boolean saveAggregates);

    /**
     * Subclasses are should retrieve necessary state to initialize their internal state.
     * @param properties Properties used to restore the internal state of the object.
     */
    void set(Map<String, String> properties);
}
