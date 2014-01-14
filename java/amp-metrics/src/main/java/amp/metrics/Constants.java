package amp.metrics;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class Constants {

    public static final String ENVBUS__INBOUND_PROCESSOR_TIMER_TEMPLATE = "amp.metrics.inboundProcessors.%s.timer";

    public static final String ENVBUS__OUTBOUND_PROCESSOR_TIMER_TEMPLATE = "amp.metrics.outboundProcessors.%s.timer";

    public static final String ENVBUS__SEND_METER_TEMPLATE = "amp.metrics.send.%s.meter";

    public static final String ENVBUS__RECEIVE_METER_TEMPLATE = "amp.metrics.receive.%s.meter";

    public static final String ENVBUS__TIME_TO_CONSUME_BY_TOPIC_TEMPLATE = "amp.metrics.consumption.%s.timer";

    public static final String ENVBUS__PROCESSOR_ERRORS_TEMPLATE = "amp.metrics.processingError.%s.count";

    public static final String ENVBUS__TOTAL_MESSAGES_SENT_NAME = "amp.metrics.totalSent";

    public static final String ENVBUS__TOTAL_MESSAGES_RECEIVED_NAME = "amp.metrics.totalSent";

    public static final String PROC__TIMES_CANCELLED_TEMPLATE = "amp.metrics.processors.cancelled.%s.count";

    public static final String PROC__TIMES_CALLED_TEMPLATE = "amp.metrics.processors.%s.count";

    public static final String PROC__TIME_TO_EXECUTE_TEMPLATE = "amp.metrics.processors.%s.timer";
}
