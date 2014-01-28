package amp.topology.protocols.rabbit.topology;

import amp.topology.global.impl.BaseConsumerGroup;
import amp.topology.global.filtering.RouteRequirements;
import amp.topology.protocols.rabbit.management.Cluster;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class RabbitConsumerGroup
        extends BaseConsumerGroup<RabbitConsumerPartition> {

    Cluster cluster;

    public void createTopicSubscription(){

    }

    public void removeTopicSubscription(){

    }

    @Override
    public Collection<RabbitConsumerPartition> filter(RouteRequirements requirements) {

        ArrayList<RabbitConsumerPartition> filteredPartitions = Lists.newArrayList();

        for (RabbitConsumerPartition subscription : this.getPartitions()){


        }

        return filteredPartitions;
    }

    @Override
    public void setup() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void cleanup() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    //TODO: Finish
    @Override
    public void set(Map<String, String> properties) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<String, String> getExtensionProperties() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
