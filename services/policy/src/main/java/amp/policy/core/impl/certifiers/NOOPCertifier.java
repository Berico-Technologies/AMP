package amp.policy.core.impl.certifiers;

import amp.policy.core.impl.PolicyCertifier;
import cmf.bus.Envelope;

/**
 * Does nothing!
 */
public class NOOPCertifier implements PolicyCertifier {

    @Override
    public void certify(Envelope e) {}
}
