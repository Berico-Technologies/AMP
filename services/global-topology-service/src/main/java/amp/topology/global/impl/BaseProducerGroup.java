package amp.topology.global.impl;

import amp.topology.global.ProducerGroup;

/**
 * Represents a BaseGroup for INBOUND traffic.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public abstract class BaseProducerGroup<PARTITION extends BasePartition>
        extends BaseGroup<PARTITION>
        implements ProducerGroup<PARTITION> {}
