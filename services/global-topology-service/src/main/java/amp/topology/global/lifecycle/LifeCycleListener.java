package amp.topology.global.lifecycle;

import amp.topology.global.Connector;

/**
 * Defines the lifecycle events handlers will be notified of when changes occur.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public interface LifeCycleListener<TOPOLOGY_ENTITY> {

    /**
     * Notify when added.
     * @param entity
     */
    void onAdded(TOPOLOGY_ENTITY entity);

    /**
     * Notify when removed.
     * @param entity
     */
    void onRemoved(TOPOLOGY_ENTITY entity);

    /**
     * Adds the notion of listening to state changes.
     * @param <TOPOLOGY_ENTITY>  ENTITY CHANGING
     * @param <STATE_REPRESENTATION> ENUM WITH THE VALID STATES
     */
    public interface StateChangeListener<TOPOLOGY_ENTITY, STATE_REPRESENTATION extends Enum> extends LifeCycleListener<TOPOLOGY_ENTITY> {

        /**
         * Called when the state changes.
         * @param thisEntity The entity changing (usually this).
         * @param oldState The state the entity is moving away from.
         * @param newState The state the entity is moving to.
         * @param reasonForChange The reason why the entity has changed state.
         */
        void onStateChange(
                TOPOLOGY_ENTITY thisEntity,
                STATE_REPRESENTATION oldState,
                STATE_REPRESENTATION newState,
                String reasonForChange);
    }

    /**
     * Listening to BaseTopic lifecycle.
     */
    public interface TopicListener extends LifeCycleListener<amp.topology.global.Topic> {}

    /**
     * Listening to BaseGroup lifecycle.
     */
    public interface GroupListener extends LifeCycleListener<amp.topology.global.Group> {}

    /**
     * Listening to BaseConnector lifecycle.
     */
    public interface ConnectorListener extends StateChangeListener<Connector, Connector.ConnectorStates> {}

    /**
     * Listening to BasePartition lifecycle.
     */
    public interface PartitionListener extends StateChangeListener<amp.topology.global.Partition, amp.topology.global.Partition.PartitionStates> {}
}
