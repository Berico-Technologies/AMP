package amp.topology.global.impl;

import amp.topology.global.TopicConfiguration;
import amp.topology.global.TopicRegistry;
import amp.topology.global.exceptions.TopologyConfigurationNotExistException;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class InMemoryTopicRegistry implements TopicRegistry {
    @Override
    public TopicConfiguration get(String id) throws TopologyConfigurationNotExistException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean exists(String id) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void register(TopicConfiguration topicConfiguration) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void unregister(String id) throws TopologyConfigurationNotExistException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Iterable<TopicConfiguration> entries() throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long lastModified() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
