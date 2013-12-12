package amp.topology.client.integration;

import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.berico.test.RequireProperties;
import com.berico.test.TestProperties;

import amp.rabbit.topology.RoutingInfo;
import amp.topology.client.HttpRoutingInfoRetriever;
import amp.topology.client.IRoutingInfoRetriever;
import amp.topology.client.JsonRoutingInfoSerializer;
import amp.topology.client.TestUtils;
import amp.utility.http.BasicAuthHttpClientProvider;
import amp.utility.http.HttpClientProvider;

public class BasicAuthIntegrationTest {

	private static final Logger logger = LoggerFactory.getLogger(BasicAuthIntegrationTest.class);
	
	 @Rule
	 public TestProperties testProperties = new TestProperties();
	
	@Test
	@RequireProperties({
		"amp.gtc.test.hostname",
		"amp.gtc.test.basic.username",
		"amp.gtc.test.basic.password",
		"amp.gtc.test.port",
		"amp.gtc.test.url",
		"amp.gtc.test.event"
	})
	public void hit_server_with_basic_auth() throws Exception {
		
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
		
		RoutingInfo routingInfo = routingInfoRetriever.retrieveRoutingInfo(eventType);
		
		TestUtils.dumpRoutingInfoToLogger(routingInfo);
		
		assertTrue(true);
	}
}
