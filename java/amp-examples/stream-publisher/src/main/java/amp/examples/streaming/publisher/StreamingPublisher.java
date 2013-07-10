package amp.examples.streaming.publisher;

import amp.eventing.streaming.DefaultStreamingBus;
import amp.examples.streaming.common.ModernMajorGeneralMessage;
import cmf.eventing.patterns.streaming.IEventStream;
import cmf.eventing.patterns.streaming.IStreamingEventBus;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;

public class StreamingPublisher {

    /**
     * Demonstrates how to publish a chunked sequence of messages or using the eventStream to publish for you.
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        ClassPathXmlApplicationContext injector = new ClassPathXmlApplicationContext("classpath:META-INF/spring/eventBusContext.xml");

        IStreamingEventBus streamingEventBus = injector.getBean("eventBus", DefaultStreamingBus.class);

        ArrayList<ModernMajorGeneralMessage> streamMessages = new ArrayList<ModernMajorGeneralMessage>();
        streamMessages.add(new ModernMajorGeneralMessage("I am "));
        streamMessages.add(new ModernMajorGeneralMessage("the very "));
        streamMessages.add(new ModernMajorGeneralMessage("model of "));
        streamMessages.add(new ModernMajorGeneralMessage("a Modern "));
        streamMessages.add(new ModernMajorGeneralMessage("Major-General, "));
        streamMessages.add(new ModernMajorGeneralMessage("I've information "));
        streamMessages.add(new ModernMajorGeneralMessage("vegetable, animal, "));
        streamMessages.add(new ModernMajorGeneralMessage("and mineral, "));
        streamMessages.add(new ModernMajorGeneralMessage("I know the kings of England, "));
        streamMessages.add(new ModernMajorGeneralMessage("and I quote the fights historical, "));
        streamMessages.add(new ModernMajorGeneralMessage("From Marathon to Waterloo, "));
        streamMessages.add(new ModernMajorGeneralMessage("in order categorical; "));


        IEventStream stream = streamingEventBus.createStream(ModernMajorGeneralMessage.class.getCanonicalName());
        stream.setBatchLimit(2);

        for (ModernMajorGeneralMessage message : streamMessages) {
            stream.publish(message);
        }
        stream.dispose();

        System.exit(0);
    }
}
