package amp.policy;

import com.bazaarvoice.dropwizard.assets.ConfiguredAssetsBundle;
import com.berico.fallwizard.SpringService;
import com.yammer.dropwizard.config.Bootstrap;


public class PolicyService extends SpringService<PolicyConfiguration> {

    public static void main(String[] args) throws Exception {

        new PolicyService().run(args);
    }

    @Override
    public void initialize(Bootstrap<PolicyConfiguration> bootstrap){

        super.initialize(bootstrap);

        bootstrap.setName("policy-service");

        bootstrap.addBundle(new ConfiguredAssetsBundle("/assets/", "/"));
    }

}