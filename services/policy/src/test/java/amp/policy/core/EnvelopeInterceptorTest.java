package amp.policy.core;

import cmf.bus.Envelope;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class EnvelopeInterceptorTest {

    @Test
    public void adjudicator_is_called_when_envelope_is_received() throws Exception {

        EnvelopeAdjudicator adj = mock(EnvelopeAdjudicator.class);

        PolicyEnforcer pe = mock(PolicyEnforcer.class);

        EnvelopeInterceptor interceptor = new EnvelopeInterceptor("id", "desc", adj, pe, new HashMap<String, String>());

        Envelope e = mock(Envelope.class);

        interceptor.handle(e);

        verify(adj).adjudicate(e, pe);
    }

}
