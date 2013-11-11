package amp.policy.core.providers;

import amp.policy.core.EnvelopeAdjudicator;
import amp.policy.core.EnvelopeInterceptor;
import amp.policy.core.InterceptorProvider;
import amp.policy.core.PolicyEnforcer;
import amp.policy.core.impl.DefaultPolicyEnforcer;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Pulls all EnvelopeInterceptors annotated with @PolicyInterceptor from the Spring Con
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

        ArrayList<EnvelopeInterceptor> interceptors = Lists.newArrayList();

        // For those who manually configure EnvelopeInterceptors
        Collection<EnvelopeInterceptor> registeredInterceptors =
                this.applicationContext.getBeansOfType(EnvelopeInterceptor.class).values();

        if (registeredInterceptors != null && registeredInterceptors.size() > 0){

            interceptors.addAll(registeredInterceptors);
        }

        // If you are using the much simpler annotation based way of wiring up an Adjudicator.
        Collection<Object> registrations = this.applicationContext.getBeansWithAnnotation(PolicyInterceptor.class).values();

        if (registrations != null && registrations.size() > 0){

            for (Object registration : registrations){

                String enforcerId = registration.getClass().getAnnotation(PolicyInterceptor.class).enforcer();

                PolicyEnforcer enforcer = null;

                if (enforcerId.equals(PolicyInterceptor.DEFAULT_VALUE)){

                    try {

                        // We are going to see if we can find an instance of the DefaultPolicyEnforcer
                        // (assuming that a custom enforcer would be of a different type).
                        // If we tried to retrieve by PolicyEnforcer, we could come up with anything!
                        enforcer = this.applicationContext.getBean(DefaultPolicyEnforcer.class);
                    }
                    catch (Exception e) {

                        LOG.info("No DefaultPolicyEnforcer defined.  Retrieving the first bean of type PolicyEnforcer.");

                        // Ok, I guess we don't have an instance of the Default, let's see if there's
                        // any kind of enforcer defined.
                        enforcer = this.applicationContext.getBean(PolicyEnforcer.class);
                    }

                } else {

                    enforcer = this.applicationContext.getBean(enforcerId, PolicyEnforcer.class);
                }

                checkNotNull(enforcer);

                if (registration instanceof EnvelopeAdjudicator){

                    EnvelopeAdjudicator adjudicator = (EnvelopeAdjudicator)registration;

                    EnvelopeInterceptor interceptor = PolicyInterceptor.Helper.createInterceptor(adjudicator, enforcer);

                    interceptors.add(interceptor);
                }
                else {

                    LOG.warn("Unable to register class '{}' adorned with @PolicyInterceptor.", registration.getClass());
                }
            }
        }

        LOG.info("Found {} envelope interceptors in the Spring Context.", interceptors.size());

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
