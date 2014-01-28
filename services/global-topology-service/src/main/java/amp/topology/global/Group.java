package amp.topology.global;

import amp.topology.global.exceptions.PartitionNotExistException;
import amp.topology.global.filtering.RouteRequirements;
import amp.topology.global.impl.BasePartition;
import com.yammer.metrics.annotation.ExceptionMetered;
import com.yammer.metrics.annotation.Metered;

import java.util.Collection;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public interface Group<PARTITION extends Partition> extends TopologyItem {

    void setGroupId(String id);

    String getGroupId();

    /**
     * Get applicable partitions based on a Request.  It is up to the BaseGroup to decide whether
     * a client should receive partitions, and which partitions those should be.
     *
     * @param requirements Client Requirements
     * @return
     */
    Collection<PARTITION> filter(RouteRequirements requirements);

    PARTITION getPartition(String id) throws PartitionNotExistException;

    Collection<PARTITION> getPartitions();

    void addPartition(PARTITION partition) throws Exception;

    void removePartition(String partitionId) throws Exception;
}
