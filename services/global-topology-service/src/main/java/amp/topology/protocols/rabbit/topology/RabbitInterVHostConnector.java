package amp.topology.protocols.rabbit.topology;

import amp.topology.global.filtering.RouteRequirements;
import amp.topology.global.impl.BaseConnector;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class RabbitInterVHostConnector extends BaseConnector<BaseRabbitPartition, RabbitConsumerPartition> {

    @Override
    public boolean verify() throws Exception {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void activate() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deactivate() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setup() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void cleanup() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
