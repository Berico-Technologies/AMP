package amp.policy.core.providers;

import amp.policy.core.Enforcer;
import amp.policy.core.EnvelopeAdjudicator;
import cmf.bus.Envelope;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class MockAdjudicator3 implements EnvelopeAdjudicator {

    @Override
    public void adjudicate(Envelope envelope, Enforcer enforcer) {

        // Do nothing.
    }
}