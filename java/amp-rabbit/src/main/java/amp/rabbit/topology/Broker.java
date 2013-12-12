package amp.rabbit.topology;

/**
 * 
 * @author Richard Clayton (Berico Technologies)
 */
public class Broker {

	/**
	 * This is used primarily to coach the ConnectionFactory
	 * to the credentials that should be used for this broker.
	 */
	protected String clusterId;
	protected String hostname;
	protected int port = 5672;
	protected String virtualHost = "/";
	protected String connectionStrategy;
	
	public Broker(){}
	
	public Broker(String hostname, int port) {
		
		this(null, hostname, port);
	}
	
	public Broker(String clusterId, String hostname, int port) {
		
		this(clusterId, hostname, port, "");
	}
	
	public Broker(String hostname, int port, String connectionStrategy) {
		
		this(null, hostname, port, connectionStrategy);
	}
	
	public Broker(String clusterId, String hostname, int port, String connectionStrategy) {
		
		this(clusterId, hostname, port, "/", connectionStrategy);
	}
	
	public Broker(String clusterId, String hostname, int port, String virtualhost, String connectionStrategy) {
		
		this.clusterId = clusterId;
		this.hostname = hostname;
		this.port = port;
		this.virtualHost = virtualhost;
		this.connectionStrategy = connectionStrategy;
	}

	public String getClusterId() {
		return clusterId;
	}
	
	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}
	
	public String getHostname() {
		return hostname;
	}
	
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	
	public int getPort() {
		return port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}

	public String getVirtualHost() {
		return virtualHost;
	}

	public void setVirtualHost(String virtualHost) {
		this.virtualHost = virtualHost;
	}
	
	public String getConnectionStrategy() {
		return connectionStrategy;
	}
	
	public void setConnectionStrategy(String strategy) {
		this.connectionStrategy = strategy;
	}
	
	@Override
	public String toString() {
		return "Broker [clusterId=" + clusterId + ", hostname=" + hostname
				+ ", port=" + port + ", vhost=" + virtualHost + ", connectionStrategy=" + connectionStrategy + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((clusterId == null) ? 0 : clusterId.hashCode());
		result = prime * result
				+ ((hostname == null) ? 0 : hostname.hashCode());
		result = prime * result 
				+ ((connectionStrategy == null) ? 0 : connectionStrategy.hashCode());
		result = prime * result + port;
		result = prime * result 
				+ ((virtualHost == null) ? 0 : virtualHost.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Broker other = (Broker) obj;
		if (clusterId == null) {
			if (other.clusterId != null)
				return false;
		} else if (!clusterId.equals(other.clusterId))
			return false;
		if (hostname == null) {
			if (other.hostname != null)
				return false;
		} else if (!hostname.equals(other.hostname))
			return false;
		if (connectionStrategy == null) {
			if (other.connectionStrategy != null)
				return false;
		} else if (!connectionStrategy.equals(other.connectionStrategy))
			return false;
		if (port != other.port)
			return false;
		if (virtualHost == null) {
			if (other.virtualHost != null)
				return false;
		} else if (!virtualHost.equals(other.virtualHost))
			return false;
		return true;
	}

	public static BrokerBuilder builder(){
		
		return new BrokerBuilder();
	}
	
	/**
	 * Simplifies construction of a Broker object.
	 * 
	 * @author Richard Clayton (Berico Technologies)
	 */
	public static class BrokerBuilder {
		
		Broker broker = new Broker();
		
		public BrokerBuilder cluster(String clusterId){
			
			broker.setClusterId(clusterId);
			
			return this;
		}
		
		public BrokerBuilder host(String host){
					
					broker.setHostname(host);
					
					return this;
				}
		
		public BrokerBuilder port(int port){
			
			broker.setPort(port);
			
			return this;
		}
		
		public BrokerBuilder vhost(String vhost){
			
			broker.setVirtualHost(vhost);
			
			return this;
		}
		
		public BrokerBuilder connectionStrategy(String strategy){
			
			broker.setConnectionStrategy(strategy);
			
			return this;
		}
		
		public Broker build(){
			
			return this.broker;
		}
	}
}