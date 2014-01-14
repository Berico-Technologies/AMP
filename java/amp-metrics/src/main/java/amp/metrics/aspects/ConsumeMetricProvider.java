package amp.metrics.aspects;

import amp.bus.IEnvelopeDispatcher;
import amp.messaging.EnvelopeHeaderConstants;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.Maps;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Gathers a number of metrics related to consuming a message.
 *
 * @author Richard Clayton (Berico Technologies)
 */
@Aspect
public class ConsumeMetricProvider {

    public static final Logger logger = LoggerFactory.getLogger(ConsumeMetricProvider.class);

    public static final String TIMER_TEMPLATE = "amp.consume.%s.timer";
    public static final String METER_TEMPLATE = "amp.consume.%s.meter";

    MetricRegistry metricsRegistry;

    @Autowired
    public ConsumeMetricProvider( MetricRegistry metricRegistry){

        this.metricsRegistry = metricRegistry;
    }

    Counter consumeCounter;

    Meter consumeMeter;

    Map<String, Timer> timerMap = Maps.newHashMap();

    Map<String, Meter> meterMap = Maps.newHashMap();

    @Around("execution(* amp.bus.IEnvelopeReceivedCallback.handleReceive(*)) and args(dispatcher)")
    public Object observeTopicReceive(ProceedingJoinPoint pjp, IEnvelopeDispatcher dispatcher) throws Throwable {

        logger.info("RECEIVING.");

        String topic = dispatcher.getEnvelope().getHeader(EnvelopeHeaderConstants.MESSAGE_TOPIC);

        Timer.Context stopwatch = getTimer(topic);

        Object value = pjp.proceed(new Object[]{dispatcher});

        stopwatch.stop();

        mark(topic);

        incrementPublishCount();

        return value;
    }

    private void incrementPublishCount(){

        if (consumeCounter == null)
            consumeCounter = metricsRegistry.counter("amp.publish.total");

        if (consumeMeter == null)
            consumeMeter = metricsRegistry.meter("amp.publish.meter");

        consumeCounter.inc();

        consumeMeter.mark();
    }

    private Timer.Context getTimer(String topic) {

        Timer timer = timerMap.get(topic);

        if (timer == null){

            timer = metricsRegistry.timer(String.format(TIMER_TEMPLATE, topic));

            timerMap.put(topic, timer);
        }

        return timer.time();
    }


    private void mark(String topic){

        Meter meter = meterMap.get(topic);

        if (meter == null){

            meter = metricsRegistry.meter(String.format(METER_TEMPLATE, topic));

            meterMap.put(topic, meter);
        }

        meter.mark();
    }

}
