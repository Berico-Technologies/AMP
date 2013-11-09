package amp.policy.core.impl.loggers;

import amp.policy.core.impl.PolicyLogger;
import cmf.bus.Envelope;
import cmf.bus.EnvelopeHeaderConstants;
import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delegates logging to the local log service.
 */
public class LocalLogger implements PolicyLogger {

    private static final Logger LOG = LoggerFactory.getLogger(LocalLogger.class);

    @Override
    public void log(Envelope e, String logType, String message) {

        String sender = Optional.of(e.getHeader(EnvelopeHeaderConstants.MESSAGE_SENDER_IDENTITY)).or("?");

        String msgType = Optional.of(e.getHeader(EnvelopeHeaderConstants.MESSAGE_TOPIC)).or("?");

        String msgId = Optional.of(e.getHeader(EnvelopeHeaderConstants.MESSAGE_ID)).or("?");

        String timeStamp = Optional.of(e.getHeader(EnvelopeHeaderConstants.ENVELOPE_RECEIPT_TIME)).or("?");

        String logMessage = String.format(
                "[id=%s, type=%s, targetSender=%s, time=%s]: %s", msgId, msgType, sender, timeStamp, message);

        if (logType.equals("WARN"))
            LOG.warn(logMessage);

        else if (logType.equals("DEBUG"))
            LOG.debug(logMessage);

        else if (logType.equals("ERROR"))
            LOG.error(logMessage);

        else
            LOG.info(logMessage);
    }
}
