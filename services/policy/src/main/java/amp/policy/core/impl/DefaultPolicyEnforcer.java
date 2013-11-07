package amp.policy.core.impl;


import amp.policy.core.PolicyEnforcer;
import cmf.bus.Envelope;
import cmf.bus.IEnvelopeBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultPolicyEnforcer implements PolicyEnforcer {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultPolicyEnforcer.class);

    @Autowired
    protected Forwarder forwarder;

    @Autowired
    protected Notifier notifier;

    @Autowired
    protected PolicyLogger logger;

    @Autowired
    protected DelayedForwarder delayedFowarder;

    @Autowired
    protected RejectionHandler rejectionHandler;

    @Autowired
    protected PolicyCertifier certifier;

    public DefaultPolicyEnforcer(){}

    public DefaultPolicyEnforcer(
            Forwarder forwarder,
            Notifier notifier,
            PolicyLogger logger,
            DelayedForwarder delayedFowarder,
            RejectionHandler rejectionHandler,
            PolicyCertifier certifier) {

        this.forwarder = forwarder;
        this.notifier = notifier;
        this.logger = logger;
        this.delayedFowarder = delayedFowarder;
        this.rejectionHandler = rejectionHandler;
        this.certifier = certifier;
    }

    @Override
    public void accept(Envelope e) {

        logger.log(e, "APPROVED", "Approved to be consumed.");

        certifier.certify(e);

        try {

            forwarder.forward(e);

        }
        catch (Exception ex){

            LOG.error("Error occurred forwarding message.", ex);
        }
    }

    @Override
    public void reject(Envelope e, String message) {

        logger.log(e, "REJECTED", String.format("Envelope was rejected: %s", message));

        rejectionHandler.handle(e, message);
    }

    @Override
    public void delay(Envelope e, long millisecondsToDelay) {

        delayedFowarder.delay(certifier, e, millisecondsToDelay);
    }

    @Override
    public void log(Envelope e, LogTypes logType, String message) {

        logger.log(e, logType.name(), message);
    }

    @Override
    public void notify(Envelope e, String entityToNotify, String message) {

        notifier.notify(e, entityToNotify, message);
    }

}
