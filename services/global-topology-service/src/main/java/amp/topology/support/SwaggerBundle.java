package amp.topology.support;

import com.wordnik.swagger.config.ConfigFactory;
import com.wordnik.swagger.config.FilterFactory;
import com.wordnik.swagger.config.ScannerFactory;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.jaxrs.config.DefaultJaxrsConfig;
import com.wordnik.swagger.jaxrs.config.DefaultJaxrsScanner;
import com.wordnik.swagger.jaxrs.listing.ApiDeclarationProvider;
import com.wordnik.swagger.jaxrs.listing.ApiListingResourceJSON;
import com.wordnik.swagger.jaxrs.listing.ResourceListingProvider;
import com.wordnik.swagger.jaxrs.reader.DefaultJaxrsApiReader;
import com.wordnik.swagger.model.ApiInfo;
import com.wordnik.swagger.reader.ClassReaders;
import com.yammer.dropwizard.assets.AssetsBundle;
import com.yammer.dropwizard.config.Environment;
import scala.Option;

import java.net.Inet4Address;
import java.net.UnknownHostException;

/**
 * Register Swagger JAX-RS JSON and UI with Dropwizard.
 *
 * Implementation was inspired by Federico Recio - https://github.com/federecio/dropwizard-swagger
 *
 * The source code was so small (literally this class) that I didn't want to put
 * it in the POM.
 *
 * TODO: Replace with Fallwizard 1.3 EnvironmentAware Bean
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class SwaggerBundle extends AssetsBundle {

    public static final String DEFAULT_HOSTNAME = "localhost";

    public static final int DEFAULT_PORT = 15677;

    public static final String SWAGGER_UI_PATH = "/api";

    public static final String TITLE = "Global Topology Service";

    public static final String DESCRIPTION =
            "The Global Topology Service is responsible for managing the endpoints and " +
            "routes in which messages flow within the AMPere architecture.";

    public static final String CONTACT = "openampere@bericotechnologies.com";

    public static final String LICENSE = "Apache 2.0";

    public static final String LICENSE_URL = "http://www.apache.org/licenses/LICENSE-2.0.html";

    public static final String TERMS_OR_SERVICE_URL = "http://www.openampere.com/terms";

    public static final String API_VERSION = "3.3.0-SNAPSHOT";


    /**
     * Initialize the bundle.
     */
    public SwaggerBundle() {

        super(SWAGGER_UI_PATH);
    }

    /**
     * Configure the JAX-RS Annotation Readers, Swagger Resources to serve the JAX-RS endpoint
     * metadata, and the Assets bundle to serve the Swagger UI.
     *
     * @param environment Dropwizard environment used to register the services.
     */
    @Override
    public void run(Environment environment) {

        // Ah, isn't Scala/Java interop so beautiful...
        FilterFactory.filter_$eq(new SwaggerParamFilter());

        environment.addResource(ApiListingResourceJSON.class);

        environment.addProvider(new ResourceListingProvider());

        environment.addProvider(new ApiDeclarationProvider());

        ScannerFactory.setScanner(new DefaultJaxrsScanner());

        ClassReaders.setReader(new DefaultJaxrsApiReader());

        SwaggerConfig config = ConfigFactory.config();

        config.setApiVersion(API_VERSION);

        ApiInfo apiInfo = new ApiInfo(TITLE, DESCRIPTION, TERMS_OR_SERVICE_URL, CONTACT, LICENSE, LICENSE_URL);

        config.setInfo(Option.apply(apiInfo));

        String hostname = DEFAULT_HOSTNAME;

        try {

            hostname = Inet4Address.getLocalHost().getHostName();

        } catch (UnknownHostException ex){

            ex.printStackTrace();
        }

        config.setBasePath(String.format("https://%s:%s", hostname, DEFAULT_PORT));

        super.run(environment);
    }
}
