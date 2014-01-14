package amp.tests.integration;

import cmf.bus.Envelope;
import cmf.eventing.IEventBus;
import cmf.eventing.IEventHandler;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Random;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class MetricProviderTests {

    protected static GenericXmlApplicationContext context;
    protected static IEventBus bus;

    public static FileSystemResource[] getConfig() throws Exception {

        return new FileSystemResource[]{
            loadResource("/config/metrics/Librato.xml"),
            loadResource("/config/metrics/Graphite.xml"),
            loadResource("/config/metrics/AllBussesConfig.xml"),
            loadResource("/config/metrics/AnubisAndTwoWaySSLRabbitConfig.xml"),
            loadResource("/config/metrics/GtsConfigSSL.xml")
        };
    }

    @BeforeClass
    public static void BeforeAllTests() throws Exception {

        context = new GenericXmlApplicationContext();

        context.load(getConfig());

        context.refresh();

        bus = (IEventBus) context.getBean("eventBus");
    }

    @AfterClass
    public static void AfterAllTests(){
        bus.dispose();
        context.close();
    }

    @Test
    public void publish_and_consume_passing_metrics_to_graphite() throws Exception {

        TestHandler handler = new TestHandler();

        bus.subscribe(handler);

        Thread.sleep(5000);

        Random random = new Random();

        TestEvent sentEvent = new TestEvent();

        for (int i = 0; i < 100; i++){

            bus.publish(sentEvent);
        }
    }

    private class TestHandler implements IEventHandler<TestEvent> {

        private TestEvent receivedEvent;

        public TestEvent getReceivedEvent(){
            return receivedEvent;
        }

        public Class<TestEvent> getEventType() {
            return TestEvent.class;
        }

        public Object handle(TestEvent event, Map<String, String> headers) {
            receivedEvent = event;
            return null;
        }

        public Object handleFailed(Envelope envelope, Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    static FileSystemResource loadResource(String path) throws URISyntaxException {

        URL location = MetricProviderTests.class.getResource(path);

        File file = new File(location.toURI());

        return new FileSystemResource(file);
    }
}
