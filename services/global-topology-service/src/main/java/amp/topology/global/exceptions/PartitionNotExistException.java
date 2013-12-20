package amp.topology.global.exceptions;

/**
 * Thrown if a Partition does not exist by a method that attempts to retrieve the Partition by ID.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class PartitionNotExistException extends Exception {

    /**
     * Initialize the Exception with the appropriate ids.
     * @param groupId Id of the Group in which the operation was committed.
     * @param partitionId Id of the partition.
     */
    public PartitionNotExistException(String groupId, String partitionId) {

        super(String.format("Group '%s' does not contain a partition with id '%s'.", groupId, partitionId));
    }
}
