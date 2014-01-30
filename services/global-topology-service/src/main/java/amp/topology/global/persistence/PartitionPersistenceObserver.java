package amp.topology.global.persistence;

import amp.topology.global.Partition;
import amp.topology.global.exceptions.PartitionNotExistException;
import amp.topology.global.impl.BasePartition;
import amp.topology.global.lifecycle.LifeCycleListener;
import amp.topology.global.lifecycle.LifeCycleObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class PartitionPersistenceObserver implements LifeCycleListener.PartitionListener {

    private static final Logger logger = LoggerFactory.getLogger(PartitionPersistenceObserver.class);

    static {

        LifeCycleObserver.addListener(new PartitionPersistenceObserver());
    }

    @Override
    public void onStateChange(
            Partition thisEntity,
            Partition.PartitionStates oldState,
            Partition.PartitionStates newState,
            String reasonForChange) {}

    @Override
    public void onAdded(Partition partition) { saveRequested(partition); }

    @Override
    public void onRemoved(Partition partition) {

        BasePartition basePartition = getPersistable(partition);

        if (basePartition != null){

            try {

                PersistenceManager.partitions().remove(partition.getPartitionId());

            } catch (PartitionNotExistException e) {

                logger.error("Partition '{}' does not exist, therefore, it can't be removed",
                    basePartition.getPartitionId());
            }
        }
        else {

            logger.warn("Partition '{}' is not persistable by the PersistenceManager.",
                basePartition.getClass().getCanonicalName());
        }
    }

    @Override
    public void saveRequested(Partition partition) {

        BasePartition basePartition = getPersistable(partition);

        if (basePartition != null){

            PersistenceManager.partitions().save(basePartition.dehydrate());
        }
        else {

            logger.warn("Partition '{}' is not persistable by the PersistenceManager.", partition.getClass().getCanonicalName());
        }
    }

    static BasePartition getPersistable(Partition partition){

        if (BasePartition.class.isAssignableFrom(partition.getClass())) return (BasePartition)partition;

        return null;
    }
}
