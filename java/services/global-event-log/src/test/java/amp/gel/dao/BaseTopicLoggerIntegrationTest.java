package amp.gel.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import amp.gel.service.TopicLogger;
import cmf.bus.IEnvelopeBus;
import cmf.eventing.IEventBus;

public class BaseTopicLoggerIntegrationTest {

	private static final Logger logger = LoggerFactory
			.getLogger(BaseTopicLoggerIntegrationTest.class);

	protected static final int NUM_EVENTS = 100;

	protected static final String TOPIC = SimplePojo.class.getCanonicalName();

	protected static final String TABLE_NAME = "test_envelope";

	@Autowired
	protected IEnvelopeBus envelopeBus;

	@Autowired
	protected IEventBus eventBus;

	@Autowired
	protected DatastoreTestHelper datastoreTestHelper;

	@Autowired
	protected DatastoreWriter datastoreWriter;

	protected TopicLogger topicLogger;

	protected void setUp() throws Exception {
		topicLogger = new TopicLogger();
		topicLogger.setEnvelopeBus(envelopeBus)
				.setDatastoreWriter(datastoreWriter).setTopic(TOPIC);
		topicLogger.start();
	}

	protected void tearDown() throws Exception {
		topicLogger.stop();
	}

	protected void test() {
//		TopicCounter topicCounter = new TopicCounter(TOPIC);
//
//        logger.debug("registering a topic counter");
//		try {
//			envelopeBus.register(topicCounter);
//		} catch (Exception e) {
//			String message = "Unable to register for messages";
//			logger.error(message, e);
//			fail(message);
//		}
//
//		try {
//			/*
//			 * Sleep to give envelope bus time to register. This can be removed
//			 * after the AMP "bug" has been fixed.
//			 */
//            logger.debug("waiting five seconds for the listener to start");
//			Thread.sleep(5000);
//		} catch (InterruptedException e) {
//			// do nothing
//		}

        //
        // jRuiz: I've commented this other stuff out because it was creating a second
        // listener, which would then get half of the POJOs I'm about to send, which
        // caused an infinite loop because it kept waiting for NUM_EVENTS, but only ever
        // got NUM_EVENTS / 2.  This is because two IRegistrations for the same topic
        // within the same process round-robin incoming events.  They don't both get
        // each event.
        //
        logger.debug("sending {} SimplePojos", NUM_EVENTS);
		for (int i = 0; i < NUM_EVENTS; i++) {
			try {
				eventBus.publish(new SimplePojo("Michael Jordan", i));
			} catch (Exception e) {
				String message = "Unable to publish messages";
				logger.error(message, e);
				fail(message);
			}
		}

        try {
            // chill out while we write all of that to the data store
            logger.debug("Waiting 5 seconds for all writers to complete.");
            Thread.sleep(5000);
        }
        catch (InterruptedException iex) {
            // do nothing
        }

//		while (topicCounter.getCount() < NUM_EVENTS) {
//			try {
//				logger.info("# of events: " + topicCounter.getCount());
//				logger.info("Missing envelopes: "
//						+ topicCounter.getMissingIds());
//
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				// do nothing
//			}
//		}

		try {
			long count = datastoreTestHelper.getRowCount(TABLE_NAME);

			assertEquals(NUM_EVENTS, count);
			logger.info("Table '" + TABLE_NAME + "' had " + count + " values");
		} catch (Exception e) {
			String message = "Unable to count entries in table";
			logger.error(message, e);
			fail(message);
		}
	}

	static public class SimplePojo {
		private String text;

		private int id;

		public SimplePojo(String text, int id) {
			super();
			this.text = text;
			this.id = id;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		@Override
		public String toString() {
			return "SimplePojo [text=" + text + ", id=" + id + "]";
		}
	}
}