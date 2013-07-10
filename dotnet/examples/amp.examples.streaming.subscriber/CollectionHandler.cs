using amp.examples.streaming.common;
using cmf.eventing.patterns.streaming;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace amp.examples.streaming.subscriber
{
    public class CollectionHandler : IStreamingCollectionHandler<ModernMajorGeneralMessage>
    {
        

        public void HandleCollection(IEnumerable<StreamingEventItem<ModernMajorGeneralMessage>> events)
        {
            Console.WriteLine("Received a collection from AMPere of size: " + events.Count());

            foreach(StreamingEventItem<ModernMajorGeneralMessage> eventItem in events)
            {
                Console.WriteLine(string.Format("Event Item: SequenceId => {0}, Position => {1}, Content => {2}",
                    eventItem.SequenceId, eventItem.Position, eventItem.Event.Content));
            }

            Console.WriteLine("Processing complete.");
        }
       
        public Type EventType
        {
            get { return typeof(ModernMajorGeneralMessage); }
        }

        public void OnPercentCollectionReceived(double percent)
        {
            Console.WriteLine("Percent of events received: " + percent + "%");
        }
    }
}
