package amp.policy.core.impl;

import cmf.bus.Envelope;

/**
 * A simple abstraction to immediately notify some entity of an event that
 * has occurred in the system.
 */
public interface Notifier {

    void notify(Envelope e, String entityToNotify, String message);
}
