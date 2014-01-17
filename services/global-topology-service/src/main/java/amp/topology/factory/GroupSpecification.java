package amp.topology.factory;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public interface GroupSpecification extends CommonSpecification {

    String getGroupId();

    boolean isConsumerGroup();

    String getProtocol();
}
