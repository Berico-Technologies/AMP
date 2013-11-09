package amp.policy.core;

import amp.policy.core.providers.MockInterceptorProvider;
import cmf.bus.IEnvelopeBus;
import org.junit.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

import java.util.Collection;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class InterceptorRegistrarTest {

    @Test
    public void registrar_loads_providers_and_registers_with_envelope_bus_on_startup(){

        GenericApplicationContext context = new GenericXmlApplicationContext("interceptorRegistrarTestContext.xml");

        Collection<EnvelopeInterceptor> interceptors = context.getBeansOfType(EnvelopeInterceptor.class).values();

        InterceptorRegistrar registrar = context.getBean(InterceptorRegistrar.class);

        for(EnvelopeInterceptor interceptor : interceptors){

            try {

                verify(registrar.envelopeBus).register(interceptor);

            } catch (Exception e){ fail(); }
        }
    }

    @Test
    public void registrar_registers_and_unregisters_interceptors_when_change_listener_is_fire_by_provider(){

        GenericApplicationContext context = new GenericXmlApplicationContext("interceptorRegistrarTestContext.xml");

        InterceptorRegistrar registrar = context.getBean(InterceptorRegistrar.class);

        IEnvelopeBus envelopeBus = registrar.envelopeBus;

        MockInterceptorProvider interceptorProvider = context.getBean(MockInterceptorProvider.class);

        assertTrue(interceptorProvider.getListeners().contains(registrar));

        EnvelopeInterceptor ei = mock(EnvelopeInterceptor.class);

        interceptorProvider.addEnvelopeInterceptor(ei);

        try {

            verify(envelopeBus).register(ei);

        } catch (Exception e){ fail(); }

        interceptorProvider.removeEnvelopeInterceptor(ei);

        try {

            verify(envelopeBus).unregister(ei);

        } catch (Exception e){ fail(); }
    }

}
