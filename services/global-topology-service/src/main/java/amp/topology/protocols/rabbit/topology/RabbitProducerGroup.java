package amp.topology.protocols.rabbit.topology;

import amp.topology.global.ProducerGroup;
import amp.topology.global.TopologyGroup;
import amp.topology.global.filtering.RouteRequirements;

import java.util.Collection;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class RabbitProducerGroup
        extends ProducerGroup<RabbitProducerPartition> {


    @Override
    public Collection<RabbitProducerPartition> filter(RouteRequirements requirements) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
