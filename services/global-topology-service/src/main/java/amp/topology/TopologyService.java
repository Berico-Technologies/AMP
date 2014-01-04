package amp.topology;

import amp.topology.support.SwaggerBundle;
import com.bazaarvoice.dropwizard.assets.ConfiguredAssetsBundle;
import com.bericotech.fallwizard.FallwizardService;
import com.yammer.dropwizard.config.Bootstrap;

public class TopologyService extends FallwizardService<TopologyConfiguration>
{
    public static void main( String[] args ) throws Exception
    {
    	new TopologyService().run(args);
    }

    @Override
	public void initialize(Bootstrap<TopologyConfiguration> bootstrap) {

		super.initialize(bootstrap);
		
		bootstrap.setName("global-topology-service");
		
		bootstrap.addBundle(new ConfiguredAssetsBundle("/assets/", "/"));

        bootstrap.addBundle(new SwaggerBundle());
	}
}
