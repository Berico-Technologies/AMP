package amp.topology.global.lifecycle;

import amp.topology.global.*;
import amp.topology.global.exceptions.ConnectorNotExistException;
import amp.topology.global.exceptions.PartitionNotExistException;
import amp.topology.global.exceptions.TopicNotExistException;
import amp.topology.global.exceptions.TopologyGroupNotExistException;
import amp.topology.global.TopologyState;

/**
 * Requirements for saving particular entities for the GTS.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public interface TopologyStatePersister<TOPOLOGY_STATE extends TopologyState, NOT_FOUND_EXCEPTION extends Throwable> {

    /**
     * Given the supplied state, save it.  This can be a new record or an update.
     * @param state state to save.
     */
    void save(TOPOLOGY_STATE state);

    /**
     * Get the topology state for the item with the provided id.
     * @param id Id of the entity to retrieve.
     * @return Topology State.
     * @throws NOT_FOUND_EXCEPTION Not Found.
     */
    TOPOLOGY_STATE get(String id) throws NOT_FOUND_EXCEPTION;

    /**
     * Remove the topology state record for a topology entity.
     * @param id Id of the entity to remove the topology state record.
     * @throws NOT_FOUND_EXCEPTION Not Found.
     */
    void remove(String id) throws NOT_FOUND_EXCEPTION;

    /**
     * Specification for the Topics.
     */
    public interface TopicStatePersister extends TopologyStatePersister<Topic.HydratedState, TopicNotExistException> {}

    /**
     * Specification for Groups.
     */
    public interface GroupStatePersister extends TopologyStatePersister<TopologyGroup.HydratedState, TopologyGroupNotExistException> {}

    /**
     * Specification for Connectors.
     */
    public interface ConnectorStatePersister extends TopologyStatePersister<Connector.HydratedState, ConnectorNotExistException> {}

    /**
     * Specification for Partitions.
     */
    public interface PartitionStatePersister extends TopologyStatePersister<Partition.HydratedState, PartitionNotExistException> {}
}

