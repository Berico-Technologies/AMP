package amp.topology.global.persistence;

import amp.topology.global.Group;
import amp.topology.global.exceptions.TopologyGroupNotExistException;
import amp.topology.global.impl.BaseGroup;
import amp.topology.global.lifecycle.LifeCycleListener;
import amp.topology.global.lifecycle.LifeCycleObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class GroupPersistenceObserver implements LifeCycleListener.GroupListener {

    private static final Logger logger = LoggerFactory.getLogger(GroupPersistenceObserver.class);

    static {

        LifeCycleObserver.addListener(new GroupPersistenceObserver());
    }

    @Override
    public void onAdded(Group group) {

        saveRequested(group);
    }

    @Override
    public void onRemoved(Group group) {

        BaseGroup baseGroup = getPersistable(group);

        if (baseGroup != null){

            try {

                PersistenceManager.groups().remove(group.getGroupId());

            } catch (TopologyGroupNotExistException ex){

                logger.error("TopologyGroup '{}' does not exist, therefore, it can't be removed", group.getGroupId());
            }
        }
        else {

            logger.warn("Group '{}' is not persistable by the PersistenceManager.", group.getClass().getCanonicalName());
        }
    }

    @Override
    public void saveRequested(Group group) {

        BaseGroup baseGroup = getPersistable(group);

        if (baseGroup != null){

            PersistenceManager.groups().save(baseGroup.dehydrate());
        }
        else {

            logger.warn("Group '{}' is not persistable by the PersistenceManager.", group.getClass().getCanonicalName());
        }
    }

    static BaseGroup getPersistable(Group group){

        if (BaseGroup.class.isAssignableFrom(group.getClass())) return (BaseGroup)group;

        return null;
    }
}
