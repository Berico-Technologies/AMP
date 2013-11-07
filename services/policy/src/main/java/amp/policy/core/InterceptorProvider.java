package amp.policy.core;

import java.util.Collection;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public interface InterceptorProvider {

    Collection<EnvelopeInterceptor> get();

    void addListener(ChangeListener listener);

    void removeListener(ChangeListener listener);

    public interface ChangeListener {

        void interceptorAdded(EnvelopeInterceptor interceptor);

        void interceptorRemoved(EnvelopeInterceptor interceptor);
    }
}
