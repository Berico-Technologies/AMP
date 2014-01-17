package amp.topology.protocols.rabbit.topology.exceptions;

import amp.rabbit.topology.Exchange;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class ExchangeDoesNotExistException extends Exception {

    private final Exchange exchange;

    public ExchangeDoesNotExistException(Exchange exchange) {

        super(String.format("Exchange '%s' does not exist.", exchange.getName()));

        this.exchange = exchange;
    }

    public Exchange getExchange() {

        return exchange;
    }
}
