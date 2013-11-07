package amp.policy.core.providers;

import amp.policy.core.EnvelopeInterceptor;
import amp.policy.core.InterceptorProvider;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class SpringInterceptorProvider implements InterceptorProvider, ApplicationContextAware {

    private static final Logger LOG = LoggerFactory.getLogger(SpringInterceptorProvider.class);

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        this.applicationContext = applicationContext;
    }

    @Override
    public Collection<EnvelopeInterceptor> get() {

        Collection<Object> registrations = this.applicationContext.getBeansWithAnnotation(AutoRegister.class).values();

        ArrayList<EnvelopeInterceptor> interceptors = Lists.newArrayList();

        if (registrations != null && registrations.size() > 0){

            for (Object registration : registrations){

                if (registration instanceof EnvelopeInterceptor){

                    EnvelopeInterceptor interceptor = (EnvelopeInterceptor)registration;

                    interceptors.add(interceptor);
                }
                else {

                    LOG.warn("Unable to register class '{}' adorned with @AutoRegister.", registration.getClass());
                }
            }
        }

        LOG.info("Found {} envelope interceptors in the Spring Context.");

        return interceptors;
    }

    @Override
    public void addListener(ChangeListener listener) {

        // This provider is immutable, so it will never notify listeners.
    }

    @Override
    public void removeListener(ChangeListener listener) {

        // This provider is immutable, so it will never notify listeners.
    }
}
