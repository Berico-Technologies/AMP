package amp.rabbit.topology;


import java.util.Arrays;
import java.util.Map;

import cmf.bus.Envelope;


public class Exchange {

    @SuppressWarnings("rawtypes")
    protected Map arguments;
    protected String exchangeType;
    protected String hostName;
    protected boolean isAutoDelete;
    protected boolean isDurable;
    protected String name;
    protected int port;
    protected String queueName;
    protected String routingKey;
    protected String virtualHost;


    public Exchange() {}

    @SuppressWarnings("rawtypes")
    public Exchange(String name, String hostName, String vHost, int port, String routingKey, String queueName,
                    String exchangeType, boolean isDurable, boolean autoDelete, Map arguments) {
        this.name = name;
        this.hostName = hostName;
        virtualHost = vHost;
        this.port = port;
        this.routingKey = routingKey;
        this.queueName = queueName;
        this.exchangeType = exchangeType;
        this.isDurable = isDurable;
        isAutoDelete = autoDelete;
        this.arguments = arguments;
    }

    @SuppressWarnings("rawtypes")
    public Map getArguments() {
        return arguments;
    }

    public String getExchangeType() {
        return exchangeType;
    }

    public String getHostName() {
        return hostName;
    }

    public boolean getIsAutoDelete() {
        return isAutoDelete;
    }

    public boolean getIsDurable() {
        return isDurable;
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    public String getQueueName() {
        return queueName;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    @SuppressWarnings("rawtypes")
    public void setArguments(Map arguments) {
        this.arguments = arguments;
    }

    public void setExchangeType(String exchangeType) {
        this.exchangeType = exchangeType;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setIsAutoDelete(boolean isAutoDelete) {
        this.isAutoDelete = isAutoDelete;
    }

    public void setIsDurable(boolean isDurable) {
        this.isDurable = isDurable;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("{");
        sb.append(String.format("Name:%s,", name));
        sb.append(String.format("HostName:%s,", hostName));
        sb.append(String.format("VirtualHost:%s,", virtualHost));
        sb.append(String.format("Port:%d,", port));
        sb.append(String.format("RoutingKey:%s,", routingKey));
        sb.append(String.format("QueueName:%s,", queueName));
        sb.append(String.format("ExchangeType:%s,", exchangeType));
        sb.append(String.format("IsDurable:%s,", isDurable));
        sb.append(String.format("IsAutoDelete:%s,", isAutoDelete));

        sb.append("}");
        return sb.toString();
    }
    
    
    
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((hostName == null) ? 0 : hostName.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Exchange other = (Exchange) obj;
		if (hostName == null) {
			if (other.hostName != null)
				return false;
		} else if (!hostName.equals(other.hostName))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
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
}
