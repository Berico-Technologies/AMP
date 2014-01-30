package amp.topology.global.impl;

import amp.topology.global.TopologyItem;

import java.util.Map;

/**
 * Represents a ton of common behavior amongst Topology items.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public abstract class BaseTopologyItem<DEHYDRATED_STATE extends TopologyState> implements TopologyItem {

    private String topicId;

    private String description;

    /**
     * Get the id of the topic.
     * @return id of this topic.
     */
    @Override
    public String getTopicId() {
        return topicId;
    }

    /**
     * Set the id of the topic.
     * @param id id of the topic.
     */
    @Override
    public void setTopicId(String id) {
        this.topicId = id;
    }

    /**
     * Get a friendly description of this item.
     * @return Friendly description.
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * Set the description of this item.
     * @param description Friendly description.
     */
    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Called when the item is instantiated.  This is an opportunity for the item to provision any resources
     * it may need to function before becoming active.
     * @throws Exception An error encountered during the setup process.
     */
    public abstract void setup() throws Exception;

    /**
     * Called when the item is being removed.  The item is given a chance to cleanup any configuration
     * it has left on the system.
     * @throws Exception An error encountered during the cleanup process.
     */
    public abstract void cleanup() throws Exception;

    /**
     * Convert the object's internal representation into something that can be stored.
     * @return
     */
    public abstract DEHYDRATED_STATE dehydrate();

    /**
     * Restore the state of the object from it's dehydrated representation.
     * @param state
     */
    public void restore(DEHYDRATED_STATE state){

        this.setTopicId(state.getTopicId());
        this.setDescription(state.getDescription());

        this.set(state.getExtensionProperties());
    }

    /**
     * Get any state in the form of key-value pairs that an implementation needs to restore it's state.
     * @return Any properties that should be persisted with the object's typical state.
     */
    public abstract Map<String, String> getExtensionProperties();
}
