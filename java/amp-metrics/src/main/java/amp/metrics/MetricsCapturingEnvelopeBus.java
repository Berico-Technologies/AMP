package amp.metrics;

import amp.bus.*;
import amp.messaging.EnvelopeHeaderConstants;
import cmf.bus.Envelope;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * A version of the DefaultEnvelopeBus that records metrics.
 *
 * - Meter: # of messages sent by topic per second.
 * - Meter: # of messages received by topic per second.
 * - Timer: Time to process inbound message by topic.
 * - Timer: Time to process outbound message by topic.
 * - Timer: Time to send message.
 * - Timer: Time to consume message by topic.
 * - Counter: # of errors encountered by Processors by Exception.
 * - Counter: Total # of messages received.
 * - Counter: Total # of messages sent.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class MetricsCapturingEnvelopeBus extends DefaultEnvelopeBus {


    MetricRegistry registry;

    ConcurrentMap<String, Timer> inboundProcessEnvelopeTimers = Maps.newConcurrentMap();

    ConcurrentMap<String, Timer> outboundProcessEnvelopeTimers = Maps.newConcurrentMap();

    ConcurrentMap<String, Timer> timeToConsumeByTopicTimers = Maps.newConcurrentMap();

    ConcurrentMap<String, Meter> inboundTopicMeters = Maps.newConcurrentMap();

    ConcurrentMap<String, Meter> outboundTopicMeters = Maps.newConcurrentMap();

    ConcurrentMap<String, Counter> processingErrorsCounters = Maps.newConcurrentMap();

    Counter totalMessagesSent;

    Counter totalMessagesReceived;

    public MetricsCapturingEnvelopeBus(ITransportProvider transportProvider, MetricRegistry registry) {

        super(transportProvider);

        this.registry = registry;
    }

    public MetricsCapturingEnvelopeBus(
            ITransportProvider transportProvider, List<IEnvelopeProcessor> inboundChain,
            List<IEnvelopeProcessor> outboundChain, MetricRegistry registry) {

        super(transportProvider, inboundChain, outboundChain);

        this.registry = registry;
    }

    @Override
    public void processEnvelope(
            EnvelopeContext context, List<IEnvelopeProcessor> processingChain, IContinuationCallback onComplete)
            throws Exception {

        Timer.Context stopwatch = startTimer(context);

        try {

            super.processEnvelope(context, processingChain, onComplete);

            stopwatch.stop();

        } catch (Exception ex){

            stopwatch.stop();

            incrementProcessingError(ex);

            throw ex;
        }
    }

    @Override
    public void send(Envelope envelope) throws Exception {

        super.send(envelope);

        incrementTotalMessagesReceived();

        markSend(envelope);
    }

    @Override
    public void handleReceive(IEnvelopeDispatcher dispatcher) {

        incrementTotalMessagesReceived();

        Timer.Context stopwatch = startTimer(dispatcher);

        super.handleReceive(dispatcher);

        stopwatch.stop();

        markReceive(dispatcher.getEnvelope());
    }

    Timer.Context startTimer(EnvelopeContext context){

        String topic = context.getEnvelope().getHeader(EnvelopeHeaderConstants.MESSAGE_TOPIC);

        ConcurrentMap<String, Timer> timerMap = selectTimerMap(context);

        String timerTemplate = selectTimerTemplate(context);

        Timer timer = retrieveTimer(registry, timerMap, topic, timerTemplate);

        return timer.time();
    }

    Timer.Context startTimer(IEnvelopeDispatcher dispatcher) {

        String topic = dispatcher.getEnvelope().getHeader(EnvelopeHeaderConstants.MESSAGE_TOPIC);

        Timer timer = retrieveTimer(
                registry, timeToConsumeByTopicTimers, topic, Constants.ENVBUS__TIME_TO_CONSUME_BY_TOPIC_TEMPLATE);

        return timer.time();
    }

    ConcurrentMap<String, Timer> selectTimerMap(EnvelopeContext context){

        return (context.getDirection() == EnvelopeContext.Directions.In)?
                inboundProcessEnvelopeTimers : outboundProcessEnvelopeTimers;
    }

    String selectTimerTemplate(EnvelopeContext context){

        return (context.getDirection() == EnvelopeContext.Directions.In)?
                Constants.ENVBUS__INBOUND_PROCESSOR_TIMER_TEMPLATE : Constants.ENVBUS__OUTBOUND_PROCESSOR_TIMER_TEMPLATE;
    }

    void markSend(Envelope envelope){

        String topic = envelope.getHeader(EnvelopeHeaderConstants.MESSAGE_TOPIC);

        retrieveMeter(registry, outboundTopicMeters, topic, Constants.ENVBUS__SEND_METER_TEMPLATE).mark();
    }

    void markReceive(Envelope envelope){

        String topic = envelope.getHeader(EnvelopeHeaderConstants.MESSAGE_TOPIC);

        retrieveMeter(registry, inboundTopicMeters, topic, Constants.ENVBUS__RECEIVE_METER_TEMPLATE).mark();
    }

    void incrementTotalMessagesSent(){

        if (totalMessagesSent == null)
            totalMessagesSent = registry.counter(Constants.ENVBUS__TOTAL_MESSAGES_SENT_NAME);

        totalMessagesSent.inc();
    }

    void incrementTotalMessagesReceived(){

        if (totalMessagesReceived == null)
            totalMessagesReceived = registry.counter(Constants.ENVBUS__TOTAL_MESSAGES_RECEIVED_NAME);

        totalMessagesReceived.inc();

    }

    void incrementProcessingError(Exception ex){

        Counter counter = retrieveCounter(
                registry, processingErrorsCounters, ex.getClass().getCanonicalName(), Constants.ENVBUS__PROCESSOR_ERRORS_TEMPLATE);

        counter.inc();
    }

    /**
     * Get the appropriate meter from the meter map, or create one if it doesn't exist.
     *
     * @param registry Registry used to create meters.
     * @param meterMap Collection of existing meters.
     * @param topic Topic to get/create a meter for.
     * @param nameTemplate Template to use when creating the name.
     * @return A meter.
     */
    static Meter retrieveMeter(
            MetricRegistry registry, ConcurrentMap<String, Meter> meterMap, String topic, String nameTemplate){

        Meter meter = meterMap.get(topic);

        if (meter == null){

            // Postpone locking unless you absolutely need it!
            synchronized (meterMap) {

                if (!meterMap.containsKey(topic)){

                    meter = registry.meter(String.format(nameTemplate, topic));
                }
                // This has to be done twice because the meter might have been added
                // before we synchronized.
                else {

                    meter = meterMap.get(topic);
                }
            }
        }

        return meter;
    }

    /**
     * Get the appropriate timer from the timer map, or create one if it doesn't exist.
     *
     * @param registry Registry used to create timers.
     * @param timerMap Collection of existing timers.
     * @param topic Topic to get/create a timer for.
     * @param nameTemplate Template to use when creating the name.
     * @return A timer.
     */
    static Timer retrieveTimer(
            MetricRegistry registry, ConcurrentMap<String, Timer> timerMap, String topic, String nameTemplate){

        Timer timer = timerMap.get(topic);

        if (timer == null){

            // Postpone locking unless you absolutely need it!
            synchronized (timerMap) {

                if (!timerMap.containsKey(topic)){

                    timer = registry.timer(String.format(nameTemplate, topic));
                }
                // This has to be done twice because the timer might have been added
                // before we synchronized.
                else {

                    timer = timerMap.get(topic);
                }
            }
        }

        return timer;
    }

    /**
     * Get the appropriate counter from the counter map, or create one if it doesn't exist.
     *
     * @param registry Registry used to create counters.
     * @param counterMap Collection of existing counters.
     * @param topic Topic to get/create a counter for.
     * @param nameTemplate Template to use when creating the name.
     * @return A counter.
     */
    static Counter retrieveCounter(
            MetricRegistry registry, ConcurrentMap<String, Counter> counterMap, String topic, String nameTemplate){

        Counter counter = counterMap.get(topic);

        if (counter == null){

            // Postpone locking unless you absolutely need it!
            synchronized (counterMap) {

                if (!counterMap.containsKey(topic)){

                    counter = registry.counter(String.format(nameTemplate, topic));
                }
                // This has to be done twice because the timer might have been added
                // before we synchronized.
                else {

                    counter = counterMap.get(topic);
                }
            }
        }

        return counter;
    }
}
