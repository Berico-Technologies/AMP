package amp.tests.integration;

import java.util.Collection;
import java.util.Map;

import org.joda.time.Duration;
import org.junit.*;
import static org.junit.Assert.*;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import cmf.bus.Envelope;
import cmf.eventing.IEventHandler;
import cmf.eventing.patterns.rpc.IRpcEventBus;

public class DefaultRpcBusTests extends DefaultEventBusTests{
	
    private static IRpcEventBus rpcBus;

    private static ClassPathXmlApplicationContext backendContext;
    private static IRpcEventBus backendBus;

	@BeforeClass
	public static void BeforeAllTests(){
		DefaultEventBusTests.BeforeAllTests();
		
		DefaultEventBusTests.bus = rpcBus = (IRpcEventBus) context.getBean("rpcEventBus");

		backendContext = new ClassPathXmlApplicationContext(getConfigFiles());
		backendBus = (IRpcEventBus) backendContext.getBean("rpcEventBus");

        try {
			backendBus.subscribe( new IEventHandler<TestRequest>(){

				public Class<TestRequest> getEventType() {
					return TestRequest.class;
				}

				public Object handle(TestRequest event, Map<String, String> headers) { 
			        backendBus.respondTo(headers, new TestResponse(event.Id));
					return null;
				}

				public Object handleFailed(Envelope env, Exception ex) {
					return null;
				}

			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@AfterClass
	public static void AfterAllTests(){
		backendBus.dispose();
		backendContext.close();
		DefaultEventBusTests.AfterAllTests();
	}
	
    @Test
    public void Should_be_able_to_send_and_receive_via_rpc() 
    {
        TestRequest request = new TestRequest(1);

        TestResponse response = rpcBus.getResponseTo(request, Duration.standardSeconds(5), TestResponse.class);

        assertNotNull("RPC Response not received within timeout period.", response);
        assertEquals("The RPC Response did not match the request.", request.Id, response.Id);
    }
    
    @Test
    @Ignore("GatherResponsesTo is not implemented yet.")
    public void Should_be_able_to_send_and_gather_via_rpc() 
    {
        TestRequest request = new TestRequest(2);

        Collection<TestResponse> responses = rpcBus.gatherResponsesTo(request, Duration.standardSeconds(5));

        assertNotNull("RPC Response not received within timeout period.", responses);
        assertEquals("The wrong number of responses were gathered.", 2, responses.size());
        for(TestResponse response : responses){
            assertEquals("The RPC Response did not match the request.", request.Id, response.Id);
        }
    }

}
