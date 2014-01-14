package amp.metrics;

import amp.messaging.IContinuationCallback;
import amp.messaging.IMessageProcessor;
import amp.messaging.MessageContext;
import amp.messaging.MessageException;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class MessageProcessorMetricCaptor extends ProcessorMetricCaptor<IMessageProcessor> implements IMessageProcessor {


    public MessageProcessorMetricCaptor(MetricRegistry registry, IMessageProcessor wrappedProcessor) {
        super(registry, wrappedProcessor);
    }

    public MessageProcessorMetricCaptor(MetricRegistry registry, IMessageProcessor wrappedProcessor, String processorName) {
        super(registry, wrappedProcessor, processorName);
    }

    @Override
    public void processMessage(MessageContext context, IContinuationCallback onComplete) throws MessageException {

        timesCalledMeter.mark();

        Timer.Context stopwatch = timeToExecute.time();

        final AtomicBoolean continueProcessing = new AtomicBoolean(false);

        this.wrappedProcessor.processMessage(context, new IContinuationCallback() {
            @Override
            public void continueProcessing() throws MessageException {

                continueProcessing.set(true);
            }
        });

        // The current implementation will call other processors upon continuation, which will not accurately
        // measure the performance of this processor.
        stopwatch.stop();

        if (continueProcessing.get())
            onComplete.continueProcessing();
        else
            timesCanceledProcessing.inc();
    }
}
