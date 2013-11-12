package amp.policy.core;


import cmf.bus.Envelope;

/**
 * Given an envelope, decide whether it should be forwarded on or rejected.
 */
public interface EnvelopeAdjudicator {

    /**
     * Determine whether an envelope should move on or be rejected, using the
     * policy enforcer to ensure that outcome.
     * @param envelope
     * @param enforcer
     */
    void adjudicate(Envelope envelope, Enforcer enforcer);
}
