package amp.topology.protocols.rabbit.topology.exceptions;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class GroupHasNoPartitionsException extends Exception {

    private final String partitionId;

    public GroupHasNoPartitionsException(String partitionId) {

        super(String.format("Partition '%s' contains a group without partitions.", partitionId));

        this.partitionId = partitionId;
    }

    public String getPartitionId() {
        return partitionId;
    }
}
