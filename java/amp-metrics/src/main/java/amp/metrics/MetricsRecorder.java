package amp.metrics;

import com.codahale.metrics.*;
import com.google.common.collect.Maps;

import java.util.concurrent.ConcurrentMap;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public abstract class MetricsRecorder {

    MetricRegistry metricRegistry;

    protected ConcurrentMap<String, Meter> meterMap = Maps.newConcurrentMap();

    protected ConcurrentMap<String, Counter> counterMap = Maps.newConcurrentMap();

    protected ConcurrentMap<String, Timer> timerMap = Maps.newConcurrentMap();

    protected ConcurrentMap<String, Histogram> histogramMap = Maps.newConcurrentMap();

    /**
     * Initialize the Metrics Recorder
     * @param metricRegistry Metric Register
     */
    protected MetricsRecorder(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    protected void mark(String meterName){

        Common.getMetric(meterMap, meterName, metricRegistry, Common.METER_MAKER).mark();
    }

    protected void mark(String meterName, long n){

        Common.getMetric(meterMap, meterName, metricRegistry, Common.METER_MAKER).mark(n);
    }

    protected Timer.Context startWatch(String timerName){

        return Common.getMetric(timerMap, timerName, metricRegistry, Common.TIMER_MAKER).time();
    }

    protected void increment(String counterName){

        Common.getMetric(counterMap, counterName, metricRegistry, Common.COUNTER_MAKER).inc();
    }

    protected void increment(String counterName, long n){

        Common.getMetric(counterMap, counterName, metricRegistry, Common.COUNTER_MAKER).inc(n);
    }

    protected void decrement(String counterName){

        Common.getMetric(counterMap, counterName, metricRegistry, Common.COUNTER_MAKER).dec();
    }

    protected void decrement(String counterName, long n){

        Common.getMetric(counterMap, counterName, metricRegistry, Common.COUNTER_MAKER).dec(n);
    }

    protected void update(String histogramName, int value) {

        Common.getMetric(histogramMap, histogramName, metricRegistry, Common.HISTOGRAM_MAKER).update(value);
    }

    protected void update(String histogramName, long value) {

        Common.getMetric(histogramMap, histogramName, metricRegistry, Common.HISTOGRAM_MAKER).update(value);
    }
}
