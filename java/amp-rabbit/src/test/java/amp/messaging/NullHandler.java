package amp.messaging;

import java.util.Map;

import cmf.bus.Envelope;

public class NullHandler implements IMessageHandler<Object> {

	@Override
	public Class<Object> getMessageType() {
		return Object.class;
	}

	@Override
	public Object handle(Object message, Map<String, String> headers) {
		return null;
	}

	@Override
	public Object handleFailed(Envelope envelope, Exception e) {
		return null;
	}

}
