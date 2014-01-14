package amp.metrics;

import amp.bus.EnvelopeContext;
import amp.bus.IContinuationCallback;
import amp.bus.IEnvelopeProcessor;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Wraps an EnvelopeProcessor, capturing metrics about that processor.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class EnvelopeProcessorMetricCaptor extends ProcessorMetricCaptor<IEnvelopeProcessor> implements IEnvelopeProcessor {


    public EnvelopeProcessorMetricCaptor(MetricRegistry registry, IEnvelopeProcessor wrappedProcessor) {
        super(registry, wrappedProcessor);
    }

    public EnvelopeProcessorMetricCaptor(MetricRegistry registry, IEnvelopeProcessor wrappedProcessor, String processorName) {
        super(registry, wrappedProcessor, processorName);
    }

    @Override
    public void processEnvelope(EnvelopeContext context, final IContinuationCallback continuation) throws Exception {

        timesCalledMeter.mark();

        Timer.Context stopwatch = timeToExecute.time();

        final AtomicBoolean continueProcessing = new AtomicBoolean(false);

        this.wrappedProcessor.processEnvelope(context, new IContinuationCallback() {
            @Override
            public void continueProcessing() throws Exception {

                continueProcessing.set(true);
            }
        });

        // The current implementation will call other processors upon continuation, which will not accurately
        // measure the performance of this processor.
        stopwatch.stop();

        if (continueProcessing.get())
            continuation.continueProcessing();
        else
            timesCanceledProcessing.inc();
    }

    @Override
    public void dispose() {

        this.wrappedProcessor.dispose();
    }
}
