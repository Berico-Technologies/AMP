package amp.policy.core.impl.forwarders;

import amp.policy.core.impl.DelayedForwarder;
import amp.policy.core.impl.Forwarder;
import amp.policy.core.impl.PolicyCertifier;
import cmf.bus.Envelope;
import cmf.bus.IEnvelopeBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Schedules an envelope to be delivered after a fixed delay.
 */
public class EnvelopeBusForwarder implements Forwarder, DelayedForwarder {

    private static final Logger LOG = LoggerFactory.getLogger(EnvelopeBusForwarder.class);

    private static final ScheduledExecutorService scheduledExecutorService =
            Executors.newSingleThreadScheduledExecutor();

    private IEnvelopeBus envelopeBus;

    @Override
    public final void forward(Envelope e) {

        try {

            this.envelopeBus.send(e);

            LOG.debug("Envelope forwarded after delay.");

        }   catch (Exception ex){

            LOG.error("Error occurred trying to forward message.", e);
        }
    }

    @Override
    public void delay(final PolicyCertifier certifier, final Envelope envelope, long delay) {

        scheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {

                certifier.certify(envelope);

                forward(envelope);
            }
        }, delay, TimeUnit.MILLISECONDS);
    }
}
