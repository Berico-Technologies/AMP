package amp.topology.global.persistence;

import amp.topology.global.Connector;
import amp.topology.global.exceptions.ConnectorNotExistException;
import amp.topology.global.impl.BaseConnector;
import amp.topology.global.lifecycle.LifeCycleListener;
import amp.topology.global.lifecycle.LifeCycleObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class ConnectorPersistenceObserver implements LifeCycleListener.ConnectorListener {

    private static final Logger logger = LoggerFactory.getLogger(ConnectorPersistenceObserver.class);

    static {

        LifeCycleObserver.addListener(new ConnectorPersistenceObserver());
    }

    @Override
    public void onStateChange(
            Connector thisEntity,
            Connector.ConnectorStates oldState,
            Connector.ConnectorStates newState,
            String reasonForChange) {}

    @Override
    public void onAdded(Connector connector) { saveRequested(connector); }

    @Override
    public void onRemoved(Connector connector) {

        BaseConnector baseConnector = getPersistable(connector);

        if (baseConnector != null){

            try {

                PersistenceManager.connectors().remove(connector.getConnectorId());

            } catch (ConnectorNotExistException e) {

                logger.error("Connector '{}' does not exist, therefore, it can't be removed",
                        connector.getConnectorId());
            }
        }
        else {

            logger.warn("Connector '{}' is not persistable by the PersistenceManager.",
                    baseConnector.getClass().getCanonicalName());
        }
    }

    @Override
    public void saveRequested(Connector connector) {

        BaseConnector baseConnector = getPersistable(connector);

        if (baseConnector != null){

            PersistenceManager.connectors().save(baseConnector.dehydrate());
        }
        else {

            logger.warn("Partition '{}' is not persistable by the PersistenceManager.", connector.getClass().getCanonicalName());
        }
    }

    static BaseConnector getPersistable(Connector connector){

        if (BaseConnector.class.isAssignableFrom(connector.getClass())) return (BaseConnector)connector;

        return null;
    }
}
