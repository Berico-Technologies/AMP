package amp.policy.core.impl.forwarders;

import amp.policy.core.impl.PolicyCertifier;
import cmf.bus.Envelope;
import cmf.bus.IEnvelopeBus;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class EnvelopeBusForwarderTest {


    @Test
    public void forwarder_sends_envelope_on_the_bus_when_forward_is_called(){

        IEnvelopeBus envelopeBus = mock(IEnvelopeBus.class);

        EnvelopeBusForwarder forwarder = new EnvelopeBusForwarder(envelopeBus);

        Envelope e = mock(Envelope.class);

        forwarder.forward(e);

        try {

            verify(envelopeBus).send(e);
        }
        catch (Exception ex){ fail(); }
    }

    @Test
    public void delayed_forwarder_sends_envelope_after_designated_delay_period() throws Exception {

        final AtomicBoolean hasBeenForwarded = new AtomicBoolean(false);

        Envelope e = mock(Envelope.class);

        IEnvelopeBus envelopeBus = mock(IEnvelopeBus.class);

        PolicyCertifier certifier = mock(PolicyCertifier.class);

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {

                hasBeenForwarded.set(true);

                return null;
            }
        }).when(envelopeBus).send(e);

        EnvelopeBusForwarder forwarder = new EnvelopeBusForwarder(envelopeBus);

        InOrder inOrder = inOrder(certifier, envelopeBus);

        forwarder.delay(certifier, e, 800);

        await().atMost(1, TimeUnit.SECONDS).untilTrue(hasBeenForwarded);

        inOrder.verify(certifier).certify(e);

        inOrder.verify(envelopeBus).send(e);
    }
}
