package amp.topology.global.exceptions;

/**
 * Thrown if an attempt is made to add a Partition that already exists.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class PartitionAlreadyExistsException extends Exception {

    public PartitionAlreadyExistsException(String groupId, String partitionId) {

        super(String.format("Group '%s' already contains partition '%s'.", groupId, partitionId));
    }
}
