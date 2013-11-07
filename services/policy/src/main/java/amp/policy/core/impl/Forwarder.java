package amp.policy.core.impl;

import cmf.bus.Envelope;

/**
 * The envelope was approved, now it needs to be forwarded to the correct
 * endpoint so consumers can consume them.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public interface Forwarder {

    /**
     * Send the envelope to the correct destination.
     * @param e Envelope to send.
     */
    void forward(Envelope e);
}
