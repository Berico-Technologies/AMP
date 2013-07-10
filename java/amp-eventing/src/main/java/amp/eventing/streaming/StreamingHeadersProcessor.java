package amp.eventing.streaming;

import amp.eventing.EnvelopeHelper;
import amp.eventing.EventContext;
import amp.eventing.IContinuationCallback;
import amp.eventing.IEventProcessor;

public class StreamingHeadersProcessor implements IEventProcessor {
    @Override
    public void processEvent(EventContext context, IContinuationCallback continuation) throws Exception {
        final String endOfStreamTopic = EndOfStream.class.getCanonicalName();
        final String collectionSizeTopic = CollectionSizeNotifier.class.getCanonicalName();

        EnvelopeHelper env = new EnvelopeHelper(context.getEnvelope());

        if (context.getDirection().equals(EventContext.Directions.Out)) {

            if (env.getMessageTopic().equals(endOfStreamTopic)) {
                env.setMessageTopic(((EndOfStream)context.getEvent()).getStreamType());
            }

            if (env.getMessageTopic().equals(collectionSizeTopic)) {
                env.setMessageTopic(((CollectionSizeNotifier)context.getEvent()).getCollectionType());
            }
        }

        continuation.continueProcessing();
    }
}
