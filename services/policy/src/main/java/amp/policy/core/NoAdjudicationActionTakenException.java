package amp.policy.core;

import cmf.bus.Envelope;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class NoAdjudicationActionTakenException extends Exception {

    private final Envelope envelope;

    public NoAdjudicationActionTakenException(Envelope envelope) {

        super("No adjudication action was taken for envelope.");

        this.envelope = envelope;
    }
}
