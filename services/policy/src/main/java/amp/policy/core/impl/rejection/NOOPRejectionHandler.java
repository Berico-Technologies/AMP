package amp.policy.core.impl.rejection;

import amp.policy.core.impl.RejectionHandler;
import cmf.bus.Envelope;

/**
 * Does nothing!
 */
public class NOOPRejectionHandler implements RejectionHandler {

    @Override
    public void handle(Envelope e, String message) {}
}
