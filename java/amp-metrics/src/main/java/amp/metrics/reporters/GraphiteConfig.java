package amp.metrics.reporters;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 * When instantiated in the Spring XML Context file, this will configure reporting to Graphite.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class GraphiteConfig {

    public GraphiteConfig(MetricRegistry metricRegistry, String graphiteHost) throws UnknownHostException {

        this(metricRegistry, Inet4Address.getLocalHost().getHostName(), graphiteHost, 2003);
    }

    public GraphiteConfig(MetricRegistry metricRegistry, String clientName, String graphiteHost){

        this(metricRegistry, clientName, graphiteHost, 2003);
    }

    public GraphiteConfig(MetricRegistry metricRegistry, String clientName, String graphiteHost, int graphitePort){

        final Graphite graphite = new Graphite(new InetSocketAddress(graphiteHost, graphitePort));

        final GraphiteReporter reporter = GraphiteReporter.forRegistry(metricRegistry)
                .prefixedWith(clientName)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .filter(MetricFilter.ALL)
                .build(graphite);

        reporter.start(1, TimeUnit.SECONDS);
    }
}
