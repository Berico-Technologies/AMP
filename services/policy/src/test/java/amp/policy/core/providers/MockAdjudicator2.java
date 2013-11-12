package amp.policy.core.providers;

import amp.policy.core.Enforcer;
import amp.policy.core.EnvelopeAdjudicator;
import cmf.bus.Envelope;

/**
 * @author Richard Clayton (Berico Technologies)
 */
@PolicyInterceptor(
        value="amp.policy.Mock2",
        sender="jdoe",
        id = "abc123",
        description = "abc123",
        enforcer = "mockEnforcer",
        regInfo = "a=1&b=2&c=3"
)
public class MockAdjudicator2 implements EnvelopeAdjudicator {

    @Override
    public void adjudicate(Envelope envelope, Enforcer enforcer) {

        // Do nothing
    }
}
