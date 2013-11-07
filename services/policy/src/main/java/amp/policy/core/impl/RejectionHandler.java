package amp.policy.core.impl;

import cmf.bus.Envelope;

/**
 * The envelope has been rejected; decided what you want to do with it.
 */
public interface RejectionHandler {

    /**
     * Handle the rejection.
     * @param e Envelope that was rejected by an adjudicator.
     * @param message The reason the message was rejected.
     */
    void handle(Envelope e, String message);
}
