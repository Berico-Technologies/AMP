using cmf.eventing.patterns.streaming;
using amp.examples.streaming.common;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace amp.examples.streaming.subscriber
{
    public class ReaderHandler : IStreamingReaderHandler<ModernMajorGeneralMessage>
    {

        public string Topic
        {
            get { return typeof(ModernMajorGeneralMessage).FullName; }
        }

        public void Dispose()
        {
            Console.WriteLine("Event Stream has been closed.");
            Console.WriteLine("Hit the Enter Key to exit the program.");
        }

        public void OnCompleted()
        {
            Console.WriteLine("Notified that last event has been processed for the stream.");
            StreamingReaderSubscriber.isDone = true;
        }

        public void OnError(Exception error)
        {
            throw new NotImplementedException();
        }

        public void OnNext(StreamingEventItem<ModernMajorGeneralMessage> eventItem) 
        {
            ModernMajorGeneralMessage mmg = eventItem.Event;
            Console.WriteLine(string.Format("Message received: (sequenceId: {0}), (position: {1}), Event Value: {2}", 
                eventItem.SequenceId, eventItem.Position, mmg.Content));
        }

        public Type EventType
        {
            get { return typeof(ModernMajorGeneralMessage); }
        }
    }
}
