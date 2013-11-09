package amp.policy.core;

import cmf.bus.IEnvelopeBus;

import static org.mockito.Mockito.mock;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class MockEnvelopeBusFactory {

    public IEnvelopeBus getEnvelopeBus(){

        return mock(IEnvelopeBus.class);
    }
}
