package amp.topology.support;

import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.config.FilterBuilder;
import org.eclipse.jetty.servlets.CrossOriginFilter;

/**
 * TODO: Replace with Fallwizard 1.3 EnvironmentAware Bean
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class CrossOriginBundle implements Bundle {

    @Override
    public void initialize(Bootstrap<?> bootstrap) {}

    @Override
    public void run(Environment environment) {

        FilterBuilder filterBuilder = environment.addFilter(CrossOriginFilter.class, "/*");

        filterBuilder.setInitParam(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");

        filterBuilder.setInitParam(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
    }
}
