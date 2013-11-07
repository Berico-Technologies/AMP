package amp.policy.core.impl.loggers;

import amp.policy.core.impl.PolicyLogger;
import cmf.bus.Envelope;

/**
 * Does nothing.
 */
public class NOOPLogger implements PolicyLogger {

    @Override
    public void log(Envelope e, String logType, String message) {}
}
