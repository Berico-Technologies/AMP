package amp.policy.core;

import cmf.bus.IEnvelopeBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Collection;


/**
 * Listens for changes on the Interceptor Registry, registering and unregistering interceptors
 * on the bus.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class InterceptorRegistrar implements ApplicationContextAware, InterceptorProvider.ChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(InterceptorRegistrar.class);

    IEnvelopeBus envelopeBus;

    private ApplicationContext applicationContext;

    protected void register(EnvelopeInterceptor interceptor) {

        try {

            this.envelopeBus.register(interceptor);

        } catch (Exception e) {

            LOG.error("Failed to register listener with the bus.", e);
        }
    }

    protected void unregister(EnvelopeInterceptor interceptor) {

        try {

            this.envelopeBus.unregister(interceptor);

        } catch (Exception e){

            LOG.error("Could not unregister the listener with the bus.", e);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        this.applicationContext = applicationContext;
        this.envelopeBus = applicationContext.getBean(IEnvelopeBus.class);

        Collection<InterceptorProvider> providers =
                applicationContext.getBeansOfType(InterceptorProvider.class).values();

        for(InterceptorProvider provider : providers){

            // Register all of the interceptors
            for(EnvelopeInterceptor interceptor : provider.get()){

                register(interceptor);
            }

            provider.addListener(this);
        }
    }

    @Override
    public void interceptorAdded(EnvelopeInterceptor interceptor) {

        LOG.info("Registered interceptor: {}", interceptor.getClass());

        register(interceptor);
    }

    @Override
    public void interceptorRemoved(EnvelopeInterceptor interceptor) {

        LOG.info("Unregistered interceptor: {}", interceptor.getClass());

        unregister(interceptor);
    }
}
