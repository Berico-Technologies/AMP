package amp.topology.support;

import com.wordnik.swagger.config.ConfigFactory;
import com.wordnik.swagger.config.ScannerFactory;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.jaxrs.config.DefaultJaxrsScanner;
import com.wordnik.swagger.jaxrs.listing.ApiDeclarationProvider;
import com.wordnik.swagger.jaxrs.listing.ApiListingResourceJSON;
import com.wordnik.swagger.jaxrs.listing.ResourceListingProvider;
import com.wordnik.swagger.jaxrs.reader.DefaultJaxrsApiReader;
import com.wordnik.swagger.reader.ClassReaders;
import com.yammer.dropwizard.assets.AssetsBundle;
import com.yammer.dropwizard.config.Environment;

import java.net.Inet4Address;
import java.net.UnknownHostException;

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

    public static final String DEFAULT_HOSTNAME = "localhost";
    public static final int DEFAULT_PORT = 15677;

    public static final String SWAGGER_UI_PATH = "/swagger-ui";

    public SwaggerBundle() {

        super(SWAGGER_UI_PATH);
    }

    @Override
    public void run(Environment environment) {

        environment.addResource(ApiListingResourceJSON.class);
        environment.addProvider(new ResourceListingProvider());
        environment.addProvider(new ApiDeclarationProvider());

        ScannerFactory.setScanner(new DefaultJaxrsScanner());
        ClassReaders.setReader(new DefaultJaxrsApiReader());

        SwaggerConfig config = ConfigFactory.config();
        config.setApiVersion("3.3.0-SNAPSHOT");

        String hostname = DEFAULT_HOSTNAME;

        try {

            hostname = Inet4Address.getLocalHost().getHostName();

        } catch (UnknownHostException ex){}

        config.setBasePath(String.format("https://%s:%s", hostname, DEFAULT_PORT));

        super.run(environment);
    }
}
