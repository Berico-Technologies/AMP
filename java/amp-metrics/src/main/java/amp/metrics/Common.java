package amp.metrics;

import com.codahale.metrics.*;

import java.util.Map;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class Common {

    public static final HistogramMaker HISTOGRAM_MAKER = new HistogramMaker();

    public static final TimerMaker TIMER_MAKER = new TimerMaker();

    public static final CounterMaker COUNTER_MAKER = new CounterMaker();

    public static final MeterMaker METER_MAKER = new MeterMaker();

    public static <T> T getMetric(Map<String, T> metricMap, String context, MetricRegistry registry, MetricMaker<T> maker){

        T metric = metricMap.get(context);

        if (metric == null){

            synchronized (metricMap){

                if (metricMap.containsKey(context)){

                    metric = metricMap.get(context);
                }
                else {

                    metric = maker.make(context, registry);
                }
            }
        }

        return metric;
    }

    public interface MetricMaker<T> {

        T make(String context, MetricRegistry registry);
    }

    public static class MeterMaker implements MetricMaker<Meter> {

        @Override
        public Meter make(String context, MetricRegistry registry) {

            return registry.meter(context);
        }
    }

    public static class TimerMaker implements MetricMaker<Timer> {

        @Override
        public Timer make(String context, MetricRegistry registry) {

            return registry.timer(context);
        }
    }

    public static class HistogramMaker implements MetricMaker<Histogram> {

        @Override
        public Histogram make(String context, MetricRegistry registry) {

            return registry.histogram(context);
        }
    }

    public static class CounterMaker implements MetricMaker<Counter> {

        @Override
        public Counter make(String context, MetricRegistry registry) {

            return registry.counter(context);
        }
    }
}
