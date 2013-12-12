package amp.rabbit.topology;


public class RouteInfo {

    protected Exchange consumerExchange;
    protected Exchange producerExchange;


    public RouteInfo() {

    }

    public RouteInfo(Exchange producerExchange, Exchange consumerExchange) {
        this.producerExchange = producerExchange;
        this.consumerExchange = consumerExchange;
    }


    public Exchange getConsumerExchange() {
        return consumerExchange;
    }

    public Exchange getProducerExchange() {
        return producerExchange;
    }

    public void setConsumerExchange(Exchange exchange) {
        consumerExchange = exchange;
    }

    public void setProducerExchange(Exchange exchange) {
        producerExchange = exchange;
    }


    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder("{");

        sb.append(" \"producerExchange\" : ");
        sb.append(producerExchange.toString());
        sb.append(", \"consumerExchange\" : ");
        sb.append(consumerExchange.toString());
        sb.append("}");

        return sb.toString();
    }
}
