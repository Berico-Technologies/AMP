package amp.policy.core;

import java.util.Collection;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public interface InterceptorProvider {

    /**
     * This should be an Idempotent function!!!!
     * @return The Set of EnvelopeInterceptors registered or built by this provider.
     */
    Collection<EnvelopeInterceptor> get();

    void addListener(ChangeListener listener);

    void removeListener(ChangeListener listener);

    public interface ChangeListener {

        void interceptorAdded(EnvelopeInterceptor interceptor);

        void interceptorRemoved(EnvelopeInterceptor interceptor);
    }
}
