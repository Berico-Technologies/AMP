package amp.rabbit.topology;


import java.util.Map;

public class Exchange extends AmqpBaseModel {

	protected String exchangeType = "direct";
	
    public Exchange() {}
    
    /** 
     * ORIGINAL CONSTRUCTOR STRING:
     *              String name, String hostName, String vHost, int port, String routingKey, String queueName,
                    String exchangeType, boolean isDurable, boolean autoDelete, Map arguments
     * DELTA:
     * 	hostName DELETED,
     *  routingKey Deleted
     *  queueName Deleted
     *  
     * @param exchangeName
     * @param exchangeType
     * @param isAutoDelete
     * @param isDurable
     * @param shouldDeclare
     * @param virtualHost
     * @param arguments
     */
    public Exchange(String exchangeName, String exchangeType,
			boolean isAutoDelete, boolean isDurable, boolean shouldDeclare,
			Map<String, Object> arguments) {

		super(exchangeName, isAutoDelete, isDurable, shouldDeclare,
				arguments);
		this.exchangeType = exchangeType;
    }
    
    public String getExchangeType() {
    	return exchangeType;
    }    

    public void setExchangeType(String exchangeType) {
        this.exchangeType = exchangeType;
    }
 
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((exchangeType == null) ? 0 : exchangeType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		//Not needed as super performs this test
		//if (getClass() != obj.getClass())
		//	return false;
		Exchange other = (Exchange) obj;
		if (exchangeType == null) {
			if (other.exchangeType != null)
				return false;
		} else if (!exchangeType.equals(other.exchangeType))
			return false;
		return true;
	}

	@Override
    public String toString() {
    	return 
    		super.getCommonStringInfo()
    			.add("ExchangeType",exchangeType)
    			.toString();
    }
    
    public static ExchangeBuilder builder(){
    	
		return new ExchangeBuilder();
}

public static class ExchangeBuilder {
	
		Exchange exchange = new Exchange();
	
		public ExchangeBuilder name(String exchangeName){
			
			this.exchange.setName(exchangeName);
			
			return this;
		}
		
	public ExchangeBuilder type(String exchangeType){
	    			
			this.exchange.setExchangeType(exchangeType);
			
			return this;
		}
	
	public ExchangeBuilder isDurable(boolean isDurable){
		
		this.exchange.setDurable(isDurable);
		
		return this;
	}
	
	public ExchangeBuilder isAutoDelete(boolean isAutoDelete){
		
		this.exchange.setAutoDelete(isAutoDelete);
		
		return this;
	}
	
	public ExchangeBuilder declare(boolean trueIfShouldDeclare){
		
		this.exchange.setShouldDeclare(trueIfShouldDeclare);
		
		return this;
	}
	
	public ExchangeBuilder arguments(Map<String, Object> arguments){
		
		if (arguments != null){
		
			this.exchange.setArguments(arguments);
		}
		return this;
	}
		
	public Exchange build(){
		
		return this.exchange;
	}
}

    
}
