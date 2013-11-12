package amp.policy.core.providers;

import amp.policy.core.EnvelopeAdjudicator;
import amp.policy.core.Enforcer;
import cmf.bus.Envelope;

/**
 * @author Richard Clayton (Berico Technologies)
 */
@Intercepts("amp.policy.Mock1")
public class MockAdjudicator1 implements EnvelopeAdjudicator {

    @Override
    public void adjudicate(Envelope envelope, Enforcer enforcer) {

        // Do nothing.
    }
}
