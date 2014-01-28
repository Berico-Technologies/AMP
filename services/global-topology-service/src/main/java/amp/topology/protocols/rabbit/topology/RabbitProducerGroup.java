package amp.topology.protocols.rabbit.topology;

import amp.topology.global.impl.BaseProducerGroup;
import amp.topology.global.filtering.RouteRequirements;

import java.util.Collection;
import java.util.Map;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class RabbitProducerGroup
        extends BaseProducerGroup<RabbitProducerPartition> {


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

    // TODO: Finish
    @Override
    public void set(Map<String, String> properties) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<String, String> getExtensionProperties() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
