package amp.metrics.aspects;


import amp.messaging.EnvelopeHeaderConstants;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.Maps;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import cmf.bus.Envelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Gathers a number of metrics related to publishing a message.
 *
 * @author Richard Clayton (Berico Technologies)
 */
@Aspect
public class PublishMetricProvider {

    private static final Logger logger = LoggerFactory.getLogger(PublishMetricProvider.class);

    public static final String TIMER_TEMPLATE = "amp.publish.%s.timer";
    public static final String METER_TEMPLATE = "amp.publish.%s.meter";

    MetricRegistry metricsRegistry;

    @Autowired
    public PublishMetricProvider( MetricRegistry metricRegistry){

        this.metricsRegistry = metricRegistry;
    }

    Counter publishedCounter;

    Meter publishedMeter;

    Map<String, Timer> timerMap = Maps.newHashMap();

    Map<String, Meter> meterMap = Maps.newHashMap();

    @Around("execution(* cmf.bus.IEnvelopeBus.send(*)) and args(envelope)")
    public Object observeTopicPublish(ProceedingJoinPoint pjp, Envelope envelope) throws Throwable {

        logger.info("PUBLISHING.");

        String topic = envelope.getHeader(EnvelopeHeaderConstants.MESSAGE_TOPIC);

        Timer.Context stopwatch = getTimer(topic);

        Object value = pjp.proceed(new Object[]{envelope});

        stopwatch.stop();

        mark(topic);

        incrementPublishCount();

        return value;
    }

    private void incrementPublishCount(){

        if (publishedCounter == null)
            publishedCounter = metricsRegistry.counter("amp.publish.total");

        if (publishedMeter == null)
            publishedMeter = metricsRegistry.meter("amp.publish.meter");

        publishedCounter.inc();

        publishedMeter.mark();
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
