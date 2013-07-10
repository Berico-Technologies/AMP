package amp.examples.streaming.subscriber;

import amp.eventing.streaming.DefaultStreamingBus;
import amp.eventing.streaming.IStandardStreamingEventBus;
import amp.examples.streaming.common.ModernMajorGeneralMessage;
import cmf.eventing.patterns.streaming.IStreamingCollectionHandler;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class StreamingCollectionSubscriber {
    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext injector = new ClassPathXmlApplicationContext("classpath:META-INF/spring/eventBusContext.xml");

        IStandardStreamingEventBus streamingEventBus = injector.getBean("eventBus", DefaultStreamingBus.class);

        IStreamingCollectionHandler<ModernMajorGeneralMessage> handler = new CollectionHandler();
        streamingEventBus.subscribeToCollection(handler);

        System.in.read();
        System.exit(0);
    }
}
