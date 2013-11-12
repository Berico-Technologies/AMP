package amp.policy.core.impl;

import cmf.bus.Envelope;

/**
 * Used to mark that a message was certified by the Intercepts Service.
 */
public interface PolicyCertifier {

    /**
     * Certify the envelope by adding header entries.
     * @param e Envelope to certify.
     */
    void certify(Envelope e);
}
