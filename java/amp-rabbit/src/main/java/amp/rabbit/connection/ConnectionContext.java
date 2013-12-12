package amp.rabbit.connection;

import java.util.ArrayList;
import java.util.Collection;

import amp.rabbit.topology.BaseRoute;
import amp.rabbit.topology.Broker;
import amp.rabbit.topology.ConsumingRoute;
import amp.rabbit.topology.ProducingRoute;

import com.rabbitmq.client.Connection;

/**
 * Represents a connection with a broker, and the routes using that connection.
 * 
 * @author Richard Clayton (Berico Technologies)
 */
public class ConnectionContext {
	
	Broker activeBroker;
	ArrayList<ProducingRoute> registeredProducingRoutes = new ArrayList<ProducingRoute>();
	ArrayList<ConsumingRoute> registeredConsumingRoutes = new ArrayList<ConsumingRoute>();
	Connection connection;
	
	public ConnectionContext(Broker activeBroker,
			ProducingRoute producingRoute,
			Connection connection) {
		
		this.activeBroker = activeBroker;
		this.registeredProducingRoutes.add(producingRoute);
		this.connection = connection;
	}
	
	public ConnectionContext(Broker activeBroker,
			ConsumingRoute consumingRoute,
			Connection connection) {
		
		this.activeBroker = activeBroker;
		this.registeredConsumingRoutes.add(consumingRoute);
		this.connection = connection;
	}
	
	public ConnectionContext(Broker activeBroker,
			Collection<ProducingRoute> producingRoutes,
			Collection<ConsumingRoute> consumingRoutes, 
			Connection connection) {
		
		this.activeBroker = activeBroker;
		this.registeredProducingRoutes.addAll(producingRoutes);
		this.registeredConsumingRoutes.addAll(consumingRoutes);
		this.connection = connection;
	}

	public Broker getActiveBroker() {
		return activeBroker;
	}
	
	public ArrayList<ProducingRoute> getProducingRoutes() {
		return registeredProducingRoutes;
	}
	
	public ArrayList<ConsumingRoute> getConsumingRoutes() {
		return registeredConsumingRoutes;
	}
	
	public Connection getConnection() {
		return connection;
	}
	
	public boolean remove(BaseRoute route){
		
		if (route instanceof ProducingRoute){
			
			return registeredProducingRoutes.remove(route);
		}
		else {
			
			return registeredConsumingRoutes.remove(route);
		}
	}
	
	public boolean hasActiveRoutes(){
		
		return registeredConsumingRoutes.size() > 0 
			|| registeredProducingRoutes.size() > 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((activeBroker == null) ? 0 : activeBroker.hashCode());
		result = prime * result
				+ ((connection == null) ? 0 : connection.hashCode());
		result = prime
				* result
				+ ((registeredConsumingRoutes == null) ? 0
						: registeredConsumingRoutes.hashCode());
		result = prime
				* result
				+ ((registeredProducingRoutes == null) ? 0
						: registeredProducingRoutes.hashCode());
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
		ConnectionContext other = (ConnectionContext) obj;
		if (activeBroker == null) {
			if (other.activeBroker != null)
				return false;
		} else if (!activeBroker.equals(other.activeBroker))
			return false;
		if (connection == null) {
			if (other.connection != null)
				return false;
		} else if (!connection.equals(other.connection))
			return false;
		if (registeredConsumingRoutes == null) {
			if (other.registeredConsumingRoutes != null)
				return false;
		} else if (!registeredConsumingRoutes
				.equals(other.registeredConsumingRoutes))
			return false;
		if (registeredProducingRoutes == null) {
			if (other.registeredProducingRoutes != null)
				return false;
		} else if (!registeredProducingRoutes
				.equals(other.registeredProducingRoutes))
			return false;
		return true;
	}
}
