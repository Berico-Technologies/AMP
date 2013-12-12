using amp.messaging;
using cmf.bus;
using System;

namespace amp.eventing.streaming
{
    public class StreamingHeadersProcessor : IMessageProcessor
    {
        public void ProcessMessage(MessageContext context, Action continueProcessing)
        {
            string endOfStreamTopic = typeof(EndOfStream).FullName;
            string collectionSizeTopic = typeof(CollectionSizeNotifier).FullName;

            if (context.Direction == MessageContext.Directions.Out)
            {
                if (context.Envelope.GetMessageTopic() == endOfStreamTopic)
                {
                    context.Envelope.SetMessageTopic(((EndOfStream)context.Message).StreamType);
                }

                if (context.Envelope.GetMessageTopic() == collectionSizeTopic)
                {
                    context.Envelope.SetMessageTopic(((CollectionSizeNotifier)context.Message).CollectionType);
                }
            }

            continueProcessing();
        }
    
        public void Dispose()
        {
            //Nothing to do.
        }
    }
}
