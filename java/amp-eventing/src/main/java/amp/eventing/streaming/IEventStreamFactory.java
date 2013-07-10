package amp.eventing.streaming;

import cmf.eventing.patterns.streaming.IEventStream;

public interface IEventStreamFactory {

    void setEventBus(IStandardStreamingEventBus bus);

    void setTopic(String topic);

    IEventStream generateEventStream();
}
