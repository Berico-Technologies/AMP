package amp.topology.protocols.rabbit.management;

import java.util.Map;

/**
 * Thrown if an error occurs during the execution of a Management Task against the RabbitMQ
 * Management Endpoints in a Cluster.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class ManagementTaskFailedOnAllEndpointsException extends Exception {

    private final Map<String, Exception> exceptionsByEndpoint;

    /**
     * Initialize with the collection of errors encountered at each endpoint.
     * @param exceptionsByEndpoint Map of Endpoint to exception.
     */
    public ManagementTaskFailedOnAllEndpointsException(Map<String, Exception> exceptionsByEndpoint) {

        super(createExceptionMessage(exceptionsByEndpoint));

        this.exceptionsByEndpoint = exceptionsByEndpoint;
    }

    /**
     * Get the exception that occurred at each endpoint.
     * @return Map of Endpoint ID to Exception.
     */
    public Map<String, Exception> getExceptionsByEndpoint() {
        return exceptionsByEndpoint;
    }

    /**
     * Creates a more friendly message as to the cause of the problem(s).
     * @param exceptionsByEndpoint Map of Endpoint to Exception
     * @return Friendly Message.
     */
    private static String createExceptionMessage(Map<String, Exception> exceptionsByEndpoint){

        StringBuilder sb = new StringBuilder();

        sb.append("Management Task failed to execute on all endpoints: \n");

        for (Map.Entry<String, Exception> entry : exceptionsByEndpoint.entrySet())
            sb.append("\t").append(entry.getKey()).append(": ").append(entry.getValue().getMessage()).append("\n");

        return sb.toString();
    }
}
