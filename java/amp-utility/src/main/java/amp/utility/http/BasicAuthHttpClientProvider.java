package amp.utility.http;


import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Provides an HttpClient configured for Basic Auth.
 * 
 * @author Richard Clayton (Berico Technologies)
 */
public class BasicAuthHttpClientProvider implements HttpClientProvider {

	DefaultHttpClient httpClient;
	
	public BasicAuthHttpClientProvider(String hostname, String username, String password){
		
		httpClient = new DefaultHttpClient();
		httpClient
			.getCredentialsProvider()
			.setCredentials(
				new AuthScope(hostname, 80), 
				new UsernamePasswordCredentials(username, password));
	}
	
	@Override
	public HttpClient getClient() {
		
		return this.httpClient;
	}
}