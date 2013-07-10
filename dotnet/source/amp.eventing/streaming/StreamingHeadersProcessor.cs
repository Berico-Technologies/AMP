using cmf.bus;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace amp.eventing.streaming
{
    public class StreamingHeadersProcessor : IEventProcessor
    {
        public void ProcessEvent(EventContext context, Action continueProcessing)
        {
            string endOfStreamTopic = typeof(EndOfStream).FullName;
            string collectionSizeTopic = typeof(CollectionSizeNotifier).FullName;

            if (context.Direction == EventContext.Directions.Out)
            {
                if (context.Envelope.GetMessageTopic() == endOfStreamTopic)
                {
                    context.Envelope.SetMessageTopic(((EndOfStream)context.Event).StreamType);
                }

                if (context.Envelope.GetMessageTopic() == collectionSizeTopic)
                {
                    context.Envelope.SetMessageTopic(((CollectionSizeNotifier)context.Event).CollectionType);
                }
            }

            continueProcessing();
        }
    }
}
