package amp.topology.client;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
//import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import amp.rabbit.topology.RoutingInfo;
import amp.topology.client.integration.BasicAuthIntegrationTest;

import com.berico.test.RequireProperties;
import com.berico.test.TestProperties;

public class GlobalTopologyServiceTest {

	private static final Logger logger = LoggerFactory.getLogger(GlobalTopologyServiceTest.class);
	
	@Rule
	public TestProperties testProperties = new TestProperties();
	
	@Test(expected=RoutingInfoNotFoundException.class)
	public void gts_fails_when_it_cant_find_a_route_for_a_topic() {
		
		IRoutingInfoRetriever retriever = mock(IRoutingInfoRetriever.class);
		
		GlobalTopologyService gts = new GlobalTopologyService(retriever);
		
		gts.getRoutingInfo(TestUtils.buildRoutingHints("A nonexistent topic!"));
	}
	
	@Test
	@RequireProperties({
		"amp.gtc.test.hostname",
		"amp.gtc.test.basic.username",
		"amp.gtc.test.basic.password",
		"amp.gtc.test.port",
		"amp.gtc.test.url",
		"amp.gtc.test.event"
	})
	public void gts_returns_correct_route() throws Exception {
		
		String hostname = System.getProperty("amp.gtc.test.hostname");
		String username = System.getProperty("amp.gtc.test.basic.username");
		String password = System.getProperty("amp.gtc.test.basic.password");
		int port = Integer.parseInt(System.getProperty("amp.gtc.test.port"));
		String serviceUrlExpression = System.getProperty("amp.gtc.test.url");
		String eventType = System.getProperty("amp.gtc.test.event");
		
		logger.debug("Calling GTS with Basic Auth.");
		
		HttpClientProvider provider = 
				new BasicAuthHttpClientProvider(hostname, port, username, password);
		
		JsonRoutingInfoSerializer serializer = new JsonRoutingInfoSerializer();
		
		IRoutingInfoRetriever routingInfoRetriever = 
			new HttpRoutingInfoRetriever(provider, serviceUrlExpression, serializer);
		
		DefaultApplicationExchangeProvider fallback = new DefaultApplicationExchangeProvider();
		
		fallback.setHostname(hostname);
		fallback.setPort(port);
		fallback.setDurable(false);
		fallback.setAutoDelete(false);
		fallback.setExchangeName("amp.fallback");
		
		GlobalTopologyService gts = new GlobalTopologyService(
			routingInfoRetriever, 10 * 60 * 1000, fallback);
		
		RoutingInfo routingInfo = gts.getRoutingInfo(TestUtils.buildRoutingHints(eventType));
		
		TestUtils.dumpRoutingInfoToLogger(routingInfo);
		
		assertTrue(true);
	}

}
