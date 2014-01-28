package amp.topology.global.impl;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * To save topology constructs uniformly, we require those constructs produce *State objects representing
 * their internal state, and when supplied a State object, are capable of initializing themselves back
 * to their previous state.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class TopologyState {

    private String topicId;

    private String description;

    private Class<?> topologyItemType;

    private Map<String, String> extensionProperties = Maps.newHashMap();

    public TopologyState(Class<?> topologyItemType, String topicId, String description) {
        this.topicId = topicId;
        this.description = description;
        this.topologyItemType = topologyItemType;
    }

    public String getDescription() {
        return description;
    }

    public Class<?> getTopologyItemType() {
        return topologyItemType;
    }

    public Map<String, String> getExtensionProperties() {
        return extensionProperties;
    }

    public String getTopicId() {
        return topicId;
    }
}
