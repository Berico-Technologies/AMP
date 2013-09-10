package amp.messaging;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cmf.bus.Envelope;
import cmf.bus.EnvelopeHeaderConstants;
import cmf.bus.IEnvelopeFilterPredicate;
import cmf.bus.IRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created with IntelliJ IDEA.
 * User: jar349
 * Date: 5/1/13
 */
public class MessageRegistration implements IRegistration {

    private static final Logger LOG = LoggerFactory.getLogger(MessageRegistration.class);

    private IMessageChainProcessor _processor;
    private List<IMessageProcessor> _processingChain;
    private IMessageHandler _handler;
    private Map<String, String> _regInfo;


    @Override
    public Map<String, String> getRegistrationInfo() { return _regInfo; }

    @Override
    public IEnvelopeFilterPredicate getFilterPredicate() { return null; }


    public MessageRegistration(
            IMessageChainProcessor processor,
            List<IMessageProcessor> processingChain,
            IMessageHandler handler) {

        _processor = processor;
        _processingChain = processingChain;
        _handler = handler;

        _regInfo = new HashMap<String, String>();
        _regInfo.put(EnvelopeHeaderConstants.MESSAGE_TOPIC, _handler.getMessageType().getCanonicalName());
        _regInfo.put(EnvelopeHeaderConstants.MESSAGE_TYPE, _handler.getMessageType().getCanonicalName());
    }


    @Override
    public Object handle(final Envelope env) throws Exception {

        LOG.debug("Enter MessageRegistration # handle( Envelope env )");

        try {
            // create a context to send through the processors
            final MessageContext ctx = new MessageContext(MessageContext.Directions.In, env);

            _processor.processMessage(ctx, _processingChain, new IContinuationCallback() {

                @Override
                public void continueProcessing() throws MessageException {
                    _handler.handle(ctx.getMessage(), env.getHeaders());
                }
            });

        }
        catch (MessageException ex) {
            String message = "Failed to process an incoming envelope.";
            LOG.error(message, ex);
            throw new Exception(message, ex);
        }

        return null;
    }

    @Override
    public Object handleFailed(Envelope env, Exception ex) throws Exception {
        return null;
    }
}
