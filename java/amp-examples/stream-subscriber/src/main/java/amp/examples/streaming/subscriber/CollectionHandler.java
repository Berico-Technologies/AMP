package amp.examples.streaming.subscriber;

import amp.examples.streaming.common.ModernMajorGeneralMessage;
import cmf.bus.Envelope;
import cmf.eventing.patterns.streaming.IStreamingCollectionHandler;
import cmf.eventing.patterns.streaming.StreamingEventItem;

import java.util.Collection;
import java.util.Map;

public class CollectionHandler implements IStreamingCollectionHandler<ModernMajorGeneralMessage> {

    @Override
    public void handleCollection(Collection<StreamingEventItem<ModernMajorGeneralMessage>> events) {
        System.out.println("Received a collection from AMPere of size: " + events.size());

        for(StreamingEventItem<ModernMajorGeneralMessage> eventItem : events) {
            System.out.println("Event Item: SequenceId => " + eventItem.getSequenceId() +
                    ", Position => " + eventItem.getPosition() +
                    "\n\t Content => " + eventItem.getEvent().getContent());
        }

        System.out.println("Processing complete.");
    }

    @Override
    public void onPercentCollectionReceived(double percent) {
        System.out.println("Percent of events received: " + percent + "%");
    }

    @Override
    public Class<ModernMajorGeneralMessage> getEventType() {
        return ModernMajorGeneralMessage.class;
    }

}
