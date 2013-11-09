package amp.policy.core;


import cmf.bus.Envelope;

/**
 * This is the mechanism provided to adjudicators for enforcing their policies.
 */
public interface PolicyEnforcer {

    /**
     * When logging, these are the valid types of log entries available to adjudicators.
     */
    public enum LogTypes {

        DEBUG,
        INFO,
        WARN,
        ERROR
    }

    /**
     * Approve the envelope, forwarding it on to it's original destination.
     * @param e Envelope to approve.
     */
    void approve(Envelope e);

    /**
     * Reject the envelope, preventing it from going on to consumers.
     * @param e Envelope to reject.
     * @param message Reason for the rejection.
     */
    void reject(Envelope e, String message);

    /**
     * Accept the envelope, but delay it's forwarding for a set amount of time.
     * @param e Envelope to delay forwarding.
     * @param millisecondsToDelay milliseconds to delay.
     */
    void delay(Envelope e, long millisecondsToDelay);

    /**
     * Write a log entry (which will hopefully be aggregated to a central location
     * for review).
     * @param e Envelope context.
     * @param logType Log type.
     * @param message Custom log message.
     */
    void log(Envelope e, LogTypes logType, String message);

    /**
     * Notify a specific entity about some occurrence or violation.
     * @param e Envelope context.
     * @param entityToNotify Entity to notify.
     * @param message Custom message.
     */
    void notify(Envelope e, String entityToNotify, String message);
}
