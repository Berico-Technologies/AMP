package amp.policy.core.providers;

import amp.policy.core.Enforcer;
import cmf.bus.Envelope;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class MockEnforcer implements Enforcer {

    @Override
    public void approve(Envelope e) {}

    @Override
    public void reject(Envelope e, String message) {}

    @Override
    public void delay(Envelope e, long millisecondsToDelay) {}

    @Override
    public void log(Envelope e, LogTypes logType, String message) {}

    @Override
    public void notify(Envelope e, String entityToNotify, String message) {}

    @Override
    public void reset() {}

    @Override
    public boolean hasAdjudicated() { return true; }
}
