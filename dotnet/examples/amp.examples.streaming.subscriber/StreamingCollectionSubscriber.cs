using amp.eventing.streaming;
using amp.examples.streaming.common;
using cmf.eventing.patterns.streaming;
using Spring.Context;
using Spring.Context.Support;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace amp.examples.streaming.subscriber
{
    public class StreamingCollectionSubscriber
    {
        public static void Main(string[] args)
        {
            IApplicationContext springContainer = ContextRegistry.GetContext();

            IStandardStreamingEventBus streamingEventBus = springContainer.GetObject(
                typeof(IStandardStreamingEventBus).FullName)
                as IStandardStreamingEventBus;

            IStreamingCollectionHandler<ModernMajorGeneralMessage> handler = new CollectionHandler();
            streamingEventBus.SubscribeToCollection<ModernMajorGeneralMessage>(handler);


            Console.WriteLine("If all events have been received, hit the Enter Key to exit the program.");
            Console.Read();
            Environment.Exit(0);
        }
    }
}
