package amp.extensions.commons.builder;

import amp.commanding.DefaultCommandReceiver;
import amp.commanding.ICommandReceiver;
import amp.rabbit.IRabbitChannelFactory;
import amp.rabbit.topology.ITopologyService;
import amp.rabbit.transport.CommandableCache;
import amp.rabbit.transport.RabbitEnvelopeReceiver;

/**
 * Created with IntelliJ IDEA.
 * User: jar349
 * Date: 5/13/13
 */
public class RoutingCacheBuilder extends FluentExtension {

    private TransportBuilder transportBuilder;


    /**
     * Instantiate with reference to the parent.
     *
     * @param parent Parent Fluent.
     */
    public RoutingCacheBuilder(BusBuilder parent, TransportBuilder transportBuilder) {

        super(parent);

        this.transportBuilder = transportBuilder;
    }


    public TransportBuilder commandedCache(long expiryTimeInSeconds) throws BuilderException {

        this.transportBuilder.createCommandedCache(expiryTimeInSeconds);

        return transportBuilder;
    }
}
