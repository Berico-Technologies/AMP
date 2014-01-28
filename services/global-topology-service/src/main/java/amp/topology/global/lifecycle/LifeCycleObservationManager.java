package amp.topology.global.lifecycle;

import amp.topology.global.Connector;
import amp.topology.global.Partition;
import amp.topology.global.Topic;
import amp.topology.global.TopologyGroup;
import amp.topology.global.lifecycle.LifeCycleListener.ConnectorListener;
import amp.topology.global.lifecycle.LifeCycleListener.GroupListener;
import amp.topology.global.lifecycle.LifeCycleListener.PartitionListener;
import amp.topology.global.lifecycle.LifeCycleListener.TopicListener;
import com.google.common.collect.Sets;

import java.util.Set;

/**
 *
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class LifeCycleObservationManager {

    static Set<ConnectorListener> connectorListeners = Sets.newCopyOnWriteArraySet();

    static Set<TopicListener> topicListeners = Sets.newCopyOnWriteArraySet();

    static Set<GroupListener> groupListeners = Sets.newCopyOnWriteArraySet();

    static Set<PartitionListener> partitionListeners = Sets.newCopyOnWriteArraySet();

    public static void addListener(TopicListener listener){

        topicListeners.add(listener);
    }

    public static void addListener(GroupListener listener){

        groupListeners.add(listener);
    }

    public static void addListener(PartitionListener listener){

        partitionListeners.add(listener);
    }

    public static void addListener(ConnectorListener listener){

        connectorListeners.add(listener);
    }

    public static void removeListener(TopicListener listener){

        topicListeners.remove(listener);
    }

    public static void removeListener(GroupListener listener){

        groupListeners.remove(listener);
    }

    public static void removeListener(PartitionListener listener){

        partitionListeners.remove(listener);
    }

    public static void removeListener(ConnectorListener listener){

        connectorListeners.remove(listener);
    }

    public static void fireOnAdded(Topic topic){

        for (TopicListener listener : topicListeners) listener.onAdded(topic);
    }

    public static void fireOnAdded(Connector connector){

        for (ConnectorListener listener : connectorListeners) listener.onAdded(connector);
    }

    public static void fireOnAdded(TopologyGroup group){

        for (GroupListener listener : groupListeners) listener.onAdded(group);
    }

    public static void fireOnAdded(Partition partition){

        for (PartitionListener listener : partitionListeners) listener.onAdded(partition);
    }

    public static void fireOnRemoved(Topic topic){

        for (TopicListener listener : topicListeners) listener.onRemoved(topic);
    }

    public static void fireOnRemoved(Connector connector){

        for (ConnectorListener listener : connectorListeners) listener.onRemoved(connector);
    }

    public static void fireOnRemoved(TopologyGroup group){

        for (GroupListener listener : groupListeners) listener.onRemoved(group);
    }

    public static void fireOnRemoved(Partition partition){

        for (PartitionListener listener : partitionListeners) listener.onRemoved(partition);
    }

    public static void fireOnStateChanged(
            Partition target, Partition.PartitionStates oldState, Partition.PartitionStates newState, String reason){

        for (PartitionListener listener : partitionListeners)
            listener.onStateChange(target, oldState, newState, reason);
    }

    public static void fireOnStateChanged(
            Connector target, Connector.ConnectorStates oldState, Connector.ConnectorStates newState, String reason){

        for (ConnectorListener listener : connectorListeners)
            listener.onStateChange(target, oldState, newState, reason);
    }
}
