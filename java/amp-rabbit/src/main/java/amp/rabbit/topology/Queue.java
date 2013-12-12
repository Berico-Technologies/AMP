package amp.rabbit.topology;

import java.util.Map;

/**
 * Represents an AMQP Queue and the necessary information to declare
 * the particular queue if it does not exist.
 * 
 * The easiest way to construct a Queue is to use the builder:
 * 
 * Queue.builder()
 *   .name("my-queue")
 *   .isDurable(true)
 *   .isAutoDelete(false)
 *   .build()
 * 
 * @author Richard Clayton (Berico Technologies)
 */
public class Queue extends AmqpBaseModel {
	
	protected boolean isExclusive = false;

	public Queue(){}
	
	public Queue(
			String name, boolean isAutoDelete, boolean isDurable, 
			boolean isExclusive, boolean shouldDeclare,
			Map<String, Object> arguments) {
		
		super(name, isAutoDelete, isDurable, shouldDeclare, arguments);
		
		this.isExclusive = isExclusive;
	}
	
	public boolean isExclusive() {
		return isExclusive;
	}

	public void setExclusive(boolean isExclusive) {
		this.isExclusive = isExclusive;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (isExclusive ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Queue other = (Queue) obj;
		if (isExclusive != other.isExclusive)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Queue [isExclusive=" + isExclusive + ", name=" + name
				+ ", isAutoDelete=" + isAutoDelete + ", isDurable=" + isDurable
				+ ", shouldDeclare=" + shouldDeclare + ", arguments=" + arguments + "]";
	}

	public static QueueBuilder builder(){
		
		return new QueueBuilder();
	}
	
	public static class QueueBuilder {
		
		Queue queue = new Queue();
		
		public QueueBuilder name(String queueName){
			
			this.queue.setName(queueName);
			
			return this;
		}
		
		public QueueBuilder isExclusive(boolean trueIfExclusive){
			
			this.queue.setExclusive(trueIfExclusive);
			
			return this;
		}
		
		public QueueBuilder isDurable(boolean trueIfDurable){
					
					this.queue.setDurable(trueIfDurable);
					
					return this;
				}
		
		public QueueBuilder isAutoDelete(boolean trueIfAutoDelete){
			
			this.queue.setAutoDelete(trueIfAutoDelete);
			
			return this;
		}
		
		public QueueBuilder declare(boolean trueIfShouldDeclare){
			
			this.queue.setShouldDeclare(trueIfShouldDeclare);
			
			return this;
		}
		
		public QueueBuilder arguments(Map<String, Object> arguments){
			
			if (arguments != null){
				
				this.queue.setArguments(arguments);
			}
			return this;
		}
		
		public Queue build(){
			
			return this.queue;
		}
	}
}
