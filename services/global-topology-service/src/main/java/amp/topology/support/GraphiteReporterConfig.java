package amp.topology.support;

import com.yammer.metrics.reporting.GraphiteReporter;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @author Richard Clayton (Berico Technologies)
 */
@Configuration
public class GraphiteReporterConfig {

    public GraphiteReporterConfig(){

        GraphiteReporter.enable(10, TimeUnit.SECONDS, "192.168.19.119", 2003);
    }
}
