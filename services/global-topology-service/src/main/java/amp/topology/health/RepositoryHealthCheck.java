package amp.topology.health;

import amp.topology.core.ITopologyRepository;

import com.yammer.metrics.core.HealthCheck;

/**
 * Check to see if the Repository is responding to queries.
 * 
 * @author Richard Clayton (Berico Technologies)
 */
public class RepositoryHealthCheck extends HealthCheck {

	ITopologyRepository topologyRepository;
	
	public RepositoryHealthCheck(ITopologyRepository topologyRepository) {
		
		super("repository");
		
		this.topologyRepository = topologyRepository;
	}

	@Override
	protected Result check() throws Exception {
		
		try {
		
			// Try to pull exchanges from the Repository.
			this.topologyRepository.getExchanges();
			
			// Try to pull routes from the Repository.
			this.topologyRepository.getRoutes();
			
		} catch (Exception ex){
			
			return Result.unhealthy(ex);
		}
		
		// The assumption is that if no exceptions are thrown, we're
		// doing all right.
		return Result.healthy();
	}

}
