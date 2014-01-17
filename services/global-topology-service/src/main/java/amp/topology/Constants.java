package amp.topology;

import amp.messaging.EnvelopeHeaderConstants;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class Constants extends EnvelopeHeaderConstants {

    public static final String CONFIG_KEY_PROTOCOL = "amp.protocol";

    public static final String PROTOCOL_AMQP = "AMQP";
    public static final String PROTOCOL_WEBSTOMP = "WebStomp";

    public static final String HEADER_REQUEST_TOPO_CREATION = "amp.topology.request.createForClient";
    public static final String HEADER_PREFERRED_QUEUENAME = "amp.topology.request.prefs.queue.name";
    public static final String HEADER_PREFERRED_QUEUE_PREFIX = "amp.topology.request.prefs.queue.prefix";

    /**
     * TODO: This should be in CMF.
     */
    public static final String MESSAGE_DIRECTION = "cmf.message.direction";
}
