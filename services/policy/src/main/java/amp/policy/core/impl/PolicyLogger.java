package amp.policy.core.impl;

import cmf.bus.Envelope;

/**
 * This is not intended to be your local logger, but rather an abstraction for some logging system in which
 * policy actions are collated for auditing purposes.
 *
 * NOTE: The reason why logType is a String and not the LogTypes enum is because I want to suppress the
 * full breadth of verbs that can be used in the logging system by users.  For instance, we don't want
 * someone to use "REJECTED" or "APPROVED" because that's a Enforcer responsibility.
 */
public interface PolicyLogger {

    /**
     * Do whatever you need to with the log entry.
     * @param e Envelope that spawned the action.
     * @param logType The type of the log entry.
     * @param message Context into why the entry is being logged.
     */
    void log(Envelope e, String logType, String message);
}
