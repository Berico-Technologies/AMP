package amp.tests.integration;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.*;
import static org.junit.Assert.*;
import static com.jayway.awaitility.Awaitility.*;
import static org.hamcrest.Matchers.*;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import amp.commanding.ICommandBus;
import amp.commanding.ICommandHandler;
import amp.messaging.MessageException;

public class DefaultCommandBusTests {
    
	protected static ClassPathXmlApplicationContext context;
	protected static ICommandBus bus;
	
	@BeforeClass
	public static void BeforeAllTests(){
		context = new ClassPathXmlApplicationContext(DefaultEventBusTests.getConfigFiles());
		bus = (ICommandBus) context.getBean("commandBus");
	}
	
	@AfterClass
	public static void AfterAllTests(){
		bus.dispose();
		context.close();
	}
	
    @Test
    public void Should_be_able_to_publish_and_subscribe() throws Exception, MessageException
    {
    	TestHandler handler = new TestHandler();
        bus.onCommandReceived(handler);
        Thread.sleep(5000);
        TestEvent sentCommand = new TestEvent();

        bus.send(sentCommand);

        waitAtMost(5, TimeUnit.SECONDS).untilCall(to(handler).getReceivedCommand(), notNullValue());
        assertEquals("Received and Sent commands were not the same.", 
        		sentCommand.Id, handler.getReceivedCommand().Id);
    }

    private class TestHandler implements ICommandHandler<TestEvent>{
    	
    	private TestEvent receivedCommand;
    	
    	public TestEvent getReceivedCommand(){
    		return receivedCommand;
    	}
    	
		public Class<TestEvent> getCommandType() {
			return TestEvent.class;
		}

		public void handle(TestEvent command, Map<String, String> headers) {
			receivedCommand = command;
		}
    }
}
