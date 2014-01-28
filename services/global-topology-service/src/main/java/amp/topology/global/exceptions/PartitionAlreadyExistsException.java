package amp.topology.global.exceptions;

/**
 * Thrown if an attempt is made to add a BasePartition that already exists.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class PartitionAlreadyExistsException extends Exception {

    public PartitionAlreadyExistsException(String groupId, String partitionId) {

        super(String.format("BaseGroup '%s' already contains partition '%s'.", groupId, partitionId));
    }
}
