package amp.policy.core.impl;

import cmf.bus.Envelope;
import cmf.bus.IEnvelopeBus;

/**
 * When a message is accepted, but delayed until a certain time, it needs to be scheduled for resubmission.
 * This interface defines when the envelope should be sent.
 *
 * So why is the certifier passed to this interface?
 *
 * The thought is that if there is any time sensitive operation in the certifier, it would be
 * problematic to certify the envelope and then delay the forwarding (potentially expiring the
 * certification).  Therefore, the certifier is sent to the delayed forwarder so it can be stamped
 * right before submission.
 */
public interface DelayedForwarder {

    /**
     * Delay the message from being delivered using the specified envelope bus.
     * @param certifier Intercepts Certifier to stamp the message.
     * @param envelope Envelope to forward.
     * @param delay Delay in milliseconds.
     */
    void delay(PolicyCertifier certifier, Envelope envelope, long delay);
}
