package amp.metrics.reporters;

import com.codahale.metrics.MetricRegistry;
import com.librato.metrics.LibratoReporter;

import java.util.concurrent.TimeUnit;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class LibratoConfig {

    public LibratoConfig(MetricRegistry metricRegistry, String clientName, String username, String apikey){

        LibratoReporter.Builder libratorBuilder = LibratoReporter
                .builder(metricRegistry, username, apikey, clientName)
                .setDurationUnit(TimeUnit.MILLISECONDS)
                .setRateUnit(TimeUnit.MILLISECONDS);

        LibratoReporter.enable(libratorBuilder, 10, TimeUnit.SECONDS);
    }
}
