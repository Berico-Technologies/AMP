package amp.topology.global.persistence;

import amp.topology.global.exceptions.ConnectorNotExistException;
import amp.topology.global.exceptions.PartitionNotExistException;
import amp.topology.global.exceptions.TopicNotExistException;
import amp.topology.global.exceptions.TopologyGroupNotExistException;
import amp.topology.global.impl.*;

import java.util.Iterator;

/**
 * Requirements for saving particular entities for the GTS.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public interface TopologyStatePersister<TOPOLOGY_STATE extends TopologyState, NOT_FOUND_EXCEPTION extends Throwable> {

    /**
     * Given the supplied state, save it.  This can be a new record or an set.
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
     * Does a record with this ID exist?
     * @param id Id of the item to check for.
     * @return TRUE if it does, FALSE if it does not.
     */
    boolean exists(String id);

    /**
     * Remove the topology state record for a topology entity.
     * @param id Id of the entity to remove the topology state record.
     * @throws NOT_FOUND_EXCEPTION Not Found.
     */
    void remove(String id) throws NOT_FOUND_EXCEPTION;

    /**
     * Provide the set of ids corresponding to entities stored in the repository.
     * @return Iterable of the ids.  The implementation may use whatever mechanism it wants to provide these ids
     * (cursors, paging, providing a collection of the full set of ids, etc).
     */
    Iterator<String> recordIdIterator();

    /**
     * Specification for the Topics.
     */
    public interface TopicStatePersister extends TopologyStatePersister<BasicTopic.DehydratedState, TopicNotExistException> {}

    /**
     * Specification for Groups.
     */
    public interface GroupStatePersister extends TopologyStatePersister<BaseGroup.DehydratedState, TopologyGroupNotExistException> {}

    /**
     * Specification for Connectors.
     */
    public interface ConnectorStatePersister extends TopologyStatePersister<BaseConnector.DehydratedState, ConnectorNotExistException> {}

    /**
     * Specification for Partitions.
     */
    public interface PartitionStatePersister extends TopologyStatePersister<BasePartition.DehydratedState, PartitionNotExistException> {}
}

