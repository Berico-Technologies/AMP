package amp.eventing.streaming;

import cmf.eventing.patterns.streaming.IEventStream;

public class DefaultEventStreamFactory implements IEventStreamFactory {

    private IStandardStreamingEventBus eventBus;
    private String topic;

    @Override
    public void setEventBus(IStandardStreamingEventBus bus) {
        this.eventBus = bus;
    }

    @Override
    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public IEventStream generateEventStream() {
        return new DefaultEventStream(eventBus, topic);
    }
}
