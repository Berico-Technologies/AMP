package amp.topology.global.persistence;

import amp.topology.global.Connector;
import amp.topology.global.PersistentTestBase;
import amp.topology.global.exceptions.ConnectorNotExistException;
import amp.topology.global.impl.BaseConnector;
import org.junit.Test;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class ConnectorPersistenceObserverTest extends PersistentTestBase {

    @Test
    public void test_onSaved(){

        BaseConnector connector = mock(BaseConnector.class);

        BaseConnector.DehydratedState expectedState = mock(BaseConnector.DehydratedState.class);

        when(connector.dehydrate()).thenReturn(expectedState);

        ConnectorPersistenceObserver observer = new ConnectorPersistenceObserver();

        observer.saveRequested(connector);

        verify(PersistenceManager.connectors()).save(expectedState);
    }

    @Test
    public void test_onRemoved() throws ConnectorNotExistException {

        String expectedConnector = "ConnectorPersistenceObserverTest";

        BaseConnector connector = mock(BaseConnector.class);

        when(connector.getConnectorId()).thenReturn(expectedConnector);

        ConnectorPersistenceObserver observer = new ConnectorPersistenceObserver();

        observer.onRemoved(connector);

        verify(PersistenceManager.connectors()).remove(expectedConnector);
    }

    @Test
    public void test_getPersistable(){

        Connector shouldBeBaseConnector = mock(BaseConnector.class);

        BaseConnector shouldNotBeNull = ConnectorPersistenceObserver.getPersistable(shouldBeBaseConnector);

        assertNotNull(shouldNotBeNull);

        Connector shouldNotBeBaseConnector = mock(Connector.class);

        BaseConnector shouldBeNull = ConnectorPersistenceObserver.getPersistable(shouldNotBeBaseConnector);

        assertNull(shouldBeNull);
    }
}
