package amp.rabbit.connection;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import amp.anubis.core.NamedToken;
import amp.rabbit.topology.Broker;
import amp.utility.http.HttpClientProvider;
import amp.utility.serialization.ISerializer;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultSaslConfig;


public class TokenConnectionFactory extends BaseConnectionFactory {

    private final HttpClientProvider _httpClientFactory;
    private final String _anubisUri;
    private final ISerializer _serializer;
    private final BaseConnectionFactory _secureConnectionFactory;

    public TokenConnectionFactory(
            HttpClientProvider httpClientFactory,
            String anubisUri,
            ISerializer serializer) {
    	this(httpClientFactory, anubisUri, serializer, null);
    }

    public TokenConnectionFactory(
            HttpClientProvider httpClientFactory,
            String anubisUri,
            ISerializer serializer,
            BaseConnectionFactory secureConnectionFactory) {
       _httpClientFactory = httpClientFactory;
       _anubisUri = anubisUri;
       _serializer = serializer;
       _secureConnectionFactory = secureConnectionFactory;
    }


    @Override
	public void configureConnectionFactory(ConnectionFactory factory, 
			Broker broker) throws Exception {
        try {

            NamedToken token = this.getNamedToken();

        	super.configureConnectionFactory(factory, broker);

        	if(_secureConnectionFactory != null){
        		_secureConnectionFactory.configureConnectionFactory(factory, 
        				broker);
            }
            
            // set the username and password from the token
        	factory.setSaslConfig(DefaultSaslConfig.PLAIN);
            factory.setUsername(token.getIdentity());
            factory.setPassword(token.getToken());

        }
        catch(Exception ex) {
        	log.error("Failed to configure connection factory with token from Anubis.", ex);
        }
    }

    public NamedToken getNamedToken() throws Exception {

        // create the client and the GET method
        HttpClient client = _httpClientFactory.getClient();
        HttpGet getMethod = new HttpGet(_anubisUri);

        // get a response by executing the GET method
        HttpResponse response = client.execute(getMethod);

        // extract the string content from the response
        String content = EntityUtils.toString(response.getEntity());
        log.debug("Received the following content from Anubis: {}", content);

        // deserialize the named token and return it
        return _serializer.stringDeserialize(content, NamedToken.class);
    }
}
