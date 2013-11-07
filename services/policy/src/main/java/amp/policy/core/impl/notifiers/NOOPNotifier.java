package amp.policy.core.impl.notifiers;

import amp.policy.core.impl.Notifier;
import cmf.bus.Envelope;

/**
 * Does nothing!
 */
public class NOOPNotifier implements Notifier {

    @Override
    public void notify(Envelope e, String entityToNotify, String message) {}
}
