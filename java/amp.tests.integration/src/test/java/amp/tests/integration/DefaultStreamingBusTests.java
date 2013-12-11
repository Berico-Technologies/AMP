package amp.tests.integration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

import cmf.eventing.patterns.streaming.IStreamingCollectionHandler;
import cmf.eventing.patterns.streaming.IStreamingEventBus;
import cmf.eventing.patterns.streaming.IStreamingReaderHandler;
import cmf.eventing.patterns.streaming.IEventStream;
import cmf.eventing.patterns.streaming.StreamingEventItem;

public class DefaultStreamingBusTests extends DefaultEventBusTests {

	private static final Logger LOG = LoggerFactory.getLogger(DefaultStreamingBusTests.class);

	private static IStreamingEventBus streamingBus;

	@BeforeClass
	public static void BeforeAllTests(){
		DefaultEventBusTests.BeforeAllTests();
		DefaultEventBusTests.bus = streamingBus = (IStreamingEventBus) context.getBean("streamingEventBus");
	}
	
	@AfterClass
	public static void AfterAllTests(){
		DefaultEventBusTests.AfterAllTests();
	}
	
    @Test
    public void Should_receive_all_segments_published_as_a_chunked_sequence() throws Exception
    {
    	CountDownLatch signal = new CountDownLatch(1);
        CollectionHandler handler = new CollectionHandler(signal);
        streamingBus.subscribeToCollection(handler);
        Thread.sleep(5000);

        ArrayList<TestStreamEvent> eventSequence = new ArrayList<TestStreamEvent>();
        eventSequence.add(new TestStreamEvent(1));
        eventSequence.add(new TestStreamEvent(2));
        eventSequence.add(new TestStreamEvent(3));

        streamingBus.publishChunkedSequence(eventSequence);

        signal.await(10, TimeUnit.SECONDS);

        assertNotNull("The collection of events was not receieved before the timeout expired.", handler.ReceivedEvents);
        assertEquals(3,handler.ReceivedEvents.size());
        for(int i = 1; i <=3; i++){
        	assertEquals(i, handler.ReceivedEvents.get(i -1).getEvent().Sequence);
        }
    }

    @Test
    @Ignore
    public void Should_receive_all_events_published_as_a_stream() throws Exception
    {
    	CountDownLatch signal = new CountDownLatch(1);
        ReaderHandler handler = new ReaderHandler(signal);

        streamingBus.subscribeToReader(handler);
        Thread.sleep(5000);

        ArrayList<TestStreamEvent> streamEvents = new ArrayList<TestStreamEvent>();
        streamEvents.add(new TestStreamEvent(1));
        streamEvents.add(new TestStreamEvent(2));
        streamEvents.add(new TestStreamEvent(3));

        IEventStream stream = streamingBus.createStream(TestStreamEvent.class.getCanonicalName());
        try {
            stream.setBatchLimit(2);

            for(TestStreamEvent event : streamEvents)
            {
                stream.publish(event);
            }
        } finally {
        	stream.dispose();
        }

        signal.await(10, TimeUnit.SECONDS);

        assertNotNull("The collection of events was not receieved before the timeout expired.", handler.ReceivedEvents);
        assertEquals(3,handler.ReceivedEvents.size());
        for(int i = 1; i <=3; i++){
        	assertEquals(i, handler.ReceivedEvents.get(i-1).getEvent().Sequence);
        }
    }

    public class CollectionHandler implements IStreamingCollectionHandler<TestStreamEvent> {
        private final CountDownLatch _signal;
        public List<StreamingEventItem<TestStreamEvent>> ReceivedEvents;

        public CollectionHandler(CountDownLatch signal) {
            _signal = signal;
        }

		@Override
		public Class<TestStreamEvent> getEventType() {
			return TestStreamEvent.class;
		}

		@Override
		public void handleCollection( Collection<StreamingEventItem<TestStreamEvent>> events) {
            ReceivedEvents = new ArrayList(events);
            _signal.countDown();

            LOG.debug(String.format("Received a collection from AMPere of size: %d", events.size()));

            for(StreamingEventItem<TestStreamEvent> eventItem : events) {
            	LOG.debug(String.format("Event Item: SequenceId => %s, Position => %d, Content => %s",
                    eventItem.getSequenceId().toString(), eventItem.getPosition(), eventItem.getEvent().toString()));
            }

            LOG.debug("Processing complete.");
		}

		@Override
		public void onPercentCollectionReceived(double percent) {
        	LOG.debug(String.format("Percent of events received: %d percent", percent));
		}
    }

    public class ReaderHandler implements IStreamingReaderHandler<TestStreamEvent> {
        private final CountDownLatch _signal;
        private final ArrayList<StreamingEventItem<TestStreamEvent>> _receivedEvents = new ArrayList<StreamingEventItem<TestStreamEvent>>();

        public List<StreamingEventItem<TestStreamEvent>>  ReceivedEvents;


        public ReaderHandler(CountDownLatch signal) {
            _signal = signal;
        }

		@Override
		public void dispose() {
			 LOG.debug("Notified that last event has been processed for the stream.");
			 ReceivedEvents = _receivedEvents;
	         _signal.countDown();
		}

		@Override
		public Class<TestStreamEvent> getEventType() {
			return TestStreamEvent.class;
		}

		@Override
		public void onEventRead(StreamingEventItem<TestStreamEvent> eventItem) {
            _receivedEvents.add(eventItem);
            LOG.debug(String.format("Message received: (sequenceId: %s), (position: %d), Event Value: %s",
            		eventItem.getSequenceId().toString(), eventItem.getPosition(), eventItem.getEvent().toString()));
            
		}
    }
}
