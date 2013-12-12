package amp.rabbit.topology;

import java.util.Map;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

/**
 * Queues and Exchanges share a number of properties. The shared behavior is
 * captured here.
 * 
 * @author jmccune (Berico Technologies)
 * @author Richard Clayton (Berico Technologies)
 * 
 */
public abstract class AmqpBaseModel {

	protected String name;

	/**
	 * Delete queue/exchange when there are no consumers/queues (respectively)
	 * using the queue/exchange.
	 */
	protected boolean isAutoDelete = false;

	/** indicates that the queue/exchange persists when the server restarts */
	protected boolean isDurable = false;

	/** Indicates if the client should create the queue/exchange if it doesn't exist. */
	protected boolean shouldDeclare = true;

	protected Map<String, Object> arguments;

	public AmqpBaseModel() {
	}

	/**
	 * 
	 * @param name
	 * @param isAutoDelete
	 * @param isDurable
	 * @param shouldDeclare
	 * @param virtualHost
	 * @param arguments
	 */
	public AmqpBaseModel(String name, boolean isAutoDelete, boolean isDurable,
			boolean shouldDeclare, Map<String, Object> arguments) {

		this.name = name;
		this.isAutoDelete = isAutoDelete;
		this.isDurable = isDurable;
		this.shouldDeclare = shouldDeclare;
		this.arguments = arguments;
	}

	// -------------------------------------
	// Standard Getters/Setters
	// -------------------------------------

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isAutoDelete() {
		return isAutoDelete;
	}

	public void setAutoDelete(boolean isAutoDelete) {
		this.isAutoDelete = isAutoDelete;
	}

	public boolean isDurable() {
		return isDurable;
	}

	public void setDurable(boolean isDurable) {
		this.isDurable = isDurable;
	}

	public boolean shouldDeclare() {
		return shouldDeclare;
	}

	public void setShouldDeclare(boolean shouldDeclare) {
		this.shouldDeclare = shouldDeclare;
	}

	public Map<String, Object> getArguments() {
		return arguments;
	}

	public void setArguments(Map<String, Object> arguments) {
		this.arguments = arguments;
	}

	// -------------------------------------
	// Object Methods
	// -------------------------------------

	@Override
	public int hashCode() {
		//Use Guava version rather than our own hash
		
		// all the arguments (not the instance itself) determine equality/match
		Object argsValue = arguments == null ? null : arguments.hashCode();
		return Objects.hashCode(argsValue,isAutoDelete,
				isDurable,name,shouldDeclare);		
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;		
		if (obj == null || getClass() != obj.getClass())
			return false;
		
		AmqpBaseModel other = (AmqpBaseModel) obj;
		return
			Objects.equal(arguments,other.arguments) &&
			isAutoDelete == other.isAutoDelete &&
			isDurable == other.isDurable &&
			Objects.equal(name, other.name) &&
			shouldDeclare == other.shouldDeclare;
		
	}
	
	protected ToStringHelper getCommonStringInfo() {
		return Objects.toStringHelper(this)
		.add("name",name)		
		.add("autoDelete",isAutoDelete)
		.add("durable",isDurable)
		.add("shouldDeclare",shouldDeclare);
	}
}
