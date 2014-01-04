package amp.topology.support;

import com.wordnik.swagger.jaxrs.listing.ApiListingResourceJSON;
import com.yammer.dropwizard.assets.AssetsBundle;
import com.yammer.dropwizard.config.Environment;

/**
 * Register Swagger JAX-RS JSON and UI with Dropwizard.
 *
 * Thanks to Federico Recio - https://github.com/federecio/dropwizard-swagger
 *
 * The source code was so small (literally this class) that I didn't want to put
 * it in the POM.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class SwaggerBundle extends AssetsBundle {

    public static final String SWAGGER_UI_PATH = "/swagger-ui";

    public SwaggerBundle() {

        super(SWAGGER_UI_PATH);
    }

    @Override
    public void run(Environment environment) {

        environment.addResource(ApiListingResourceJSON.class);

        super.run(environment);
    }
}
