package amp.messaging;

public class NullMessageProcessor implements IMessageProcessor {

	@Override
	public void processMessage(MessageContext context,
			IContinuationCallback onComplete) throws MessageException {

	}

}
