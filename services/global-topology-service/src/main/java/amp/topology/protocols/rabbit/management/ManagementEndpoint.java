package amp.topology.protocols.rabbit.management;

import rabbitmq.httpclient.HttpClientProvider;
import rabbitmq.mgmt.RabbitMgmtService;

/**
 * Represents a RabbitMQ Management Console Endpoint in a Cluster.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class ManagementEndpoint {

    private HttpClientProvider httpClientProvider;

    private String hostname;

    private int port;

    private boolean useSSL = false;

    /**
     * Instantiate the Management Endpoint.
     * @param httpClientProvider Mechanism to retrieve a configured HTTP Client that can communicate with the
     *                           endpoint.
     */
    public ManagementEndpoint(String hostname, int port, boolean useSSL, HttpClientProvider httpClientProvider) {

        this.hostname = hostname;
        this.port = port;
        this.useSSL = useSSL;
        this.httpClientProvider = httpClientProvider;
    }

    /**
     * This is a pre-configured HTTP client provider for the endpoint.
     * @return Client Provider
     */
    public HttpClientProvider getHttpClientProvider() {
        return httpClientProvider;
    }

    /**
     * Get the Hostname of the Management Endpoint
     * @return Hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Get the port of the Management Endpoint
     * @return Port
     */
    public int getPort() {
        return port;
    }

    /**
     * Get the ID (hostname:port) of this Management Endpoint.
     * @return ID of the Management Endpoint.
     */
    public String getId(){

        return String.format("%s:%s", hostname, port);
    }

    /**
     * Returns an instance of the RabbitMQ Management Service.
     * @return RMQ Management Service
     */
    public RabbitMgmtService getManagementService(){

        return new RabbitMgmtService(this.hostname, this.port, this.useSSL, this.httpClientProvider).initialize();
    }
}
