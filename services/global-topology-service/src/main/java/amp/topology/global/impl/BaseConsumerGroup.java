package amp.topology.global.impl;

import amp.topology.global.ConsumerGroup;

/**
 * Represents a BaseGroup for OUTBOUND traffic.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public abstract class BaseConsumerGroup<PARTITION extends BasePartition>
        extends BaseGroup<PARTITION>
        implements ConsumerGroup<PARTITION> {}
