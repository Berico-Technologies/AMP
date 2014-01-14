package amp.metrics;

import amp.metrics.aspects.PublishMetricProvider;
import com.codahale.metrics.MetricRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author Richard Clayton (Berico Technologies)
 */
@Configuration
@EnableAspectJAutoProxy
public class MetricsConfig {

    MetricRegistry metricRegistry = new MetricRegistry();

    @Bean
    public MetricRegistry getMetricRegistry(){

        return metricRegistry;
    }

}
