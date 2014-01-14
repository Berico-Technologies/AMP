package amp.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public abstract class ProcessorMetricCaptor<T> {

    MetricRegistry registry;

    T wrappedProcessor;

    String processorName;

    Meter timesCalledMeter;

    Counter timesCanceledProcessing;

    Timer timeToExecute;

    protected ProcessorMetricCaptor(MetricRegistry registry, T wrappedProcessor) {

        this(registry, wrappedProcessor, wrappedProcessor.getClass().getCanonicalName());
    }

    protected ProcessorMetricCaptor(MetricRegistry registry, T wrappedProcessor, String processorName) {

        this.registry = registry;
        this.wrappedProcessor = wrappedProcessor;
        this.processorName = processorName;

        initialize();
    }

    private void initialize(){

        timesCalledMeter = registry.meter(
                String.format(Constants.PROC__TIMES_CALLED_TEMPLATE, processorName));

        timeToExecute = registry.timer(
                String.format(Constants.PROC__TIME_TO_EXECUTE_TEMPLATE, processorName));

        timesCanceledProcessing = registry.counter(
                String.format(Constants.PROC__TIMES_CANCELLED_TEMPLATE, processorName));
    }
}
