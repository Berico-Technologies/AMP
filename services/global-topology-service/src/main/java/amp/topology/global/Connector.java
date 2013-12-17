package amp.topology.global;

/**
 * Represents a connection between a ProducerGroup and a ConsumerGroup.  This maybe a logical connection,
 * configuration (say a routing key for an Exchange + Queue binging in AMQP), or a complex bridge
 * (protocol transition),
 *
 * @author Richard Clayton (Berico Technologies)
 */
public interface Connector<PRODUCING_PARTITION extends Partition, CONSUMING_PARTITION extends Partition> {

    String getId();

    String getDescription();

    /**
     * Verify the connection between the ProducerGroup and ConsumerGroup is identical to the
     * connector state.
     *
     * This method will be called on a schedule to ensure the route is in sync.  The method does not
     * have to be idempotent.  If the state is invalid, it may update the state and fire handlers, but
     * it must return FALSE if the state was incorrect at the beginning of the call.
     *
     * @return TRUE if the active connector state matches that actual infrastructure state. Like the
     * Partition interface, verify does not mean everything is "hunky-dory".
     * @throws Exception An exception that may have arisen while verifying the infrastructure is
     * in the correct state.
     */
    boolean verify() throws Exception;

    public enum ConnectorState {
        IN_ERROR

    }

    ProducerGroup<PRODUCING_PARTITION> getProducingGroup();

    ConsumerGroup<CONSUMING_PARTITION> getConsumingGroup();

}
