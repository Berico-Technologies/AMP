package amp.topology.global.persistence;

import amp.topology.global.Topic;
import amp.topology.global.impl.BasicTopic;
import amp.topology.global.lifecycle.LifeCycleListener;
import amp.topology.global.lifecycle.LifeCycleObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class TopicPersistenceObserver implements LifeCycleListener.TopicListener {

    private static final Logger logger = LoggerFactory.getLogger(TopicPersistenceObserver.class);

    static {

        LifeCycleObserver.addListener(new TopicPersistenceObserver());
    }

    /**
     * This is managed by the PersistentTopicRegistry.
     * @param topic
     */
    @Override
    public void onAdded(Topic topic) {}

    /**
     * This is managed by the PersistentTopicRegistry.
     * @param topic
     */
    @Override
    public void onRemoved(Topic topic) {}

    @Override
    public void saveRequested(Topic topic) {

        BasicTopic basicTopic = getPersistable(topic);

        if (basicTopic != null){

            PersistenceManager.topics().save(basicTopic.dehydrate());
        }
        else {

            logger.warn("Topic '{}' is not persistable by the PersistenceManager.", topic.getClass().getCanonicalName());
        }
    }

    static BasicTopic getPersistable(Topic topic){

        if (BasicTopic.class.isAssignableFrom(topic.getClass())) return (BasicTopic)topic;

        return null;
    }
}
