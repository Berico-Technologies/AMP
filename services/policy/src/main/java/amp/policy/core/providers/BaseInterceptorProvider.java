package amp.policy.core.providers;

import amp.policy.core.EnvelopeInterceptor;
import amp.policy.core.InterceptorProvider;
import com.google.common.collect.Lists;

import java.util.ArrayList;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Event plumbing for Interceptor Providers.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public abstract class BaseInterceptorProvider implements InterceptorProvider {

    /**
     * List of listeners.
     */
    protected ArrayList<ChangeListener> listeners = Lists.newArrayList();

    /**
     * Notify listeners that an interceptor was added.
     *
     * @param interceptor Interceptor that was added.
     */
    protected void notifyInterceptorAdded(EnvelopeInterceptor interceptor){

        for (ChangeListener listener : listeners){

            listener.interceptorAdded(interceptor);
        }
    }

    /**
     * Notify listeners that an interceptor was removed.
     *
     * @param interceptor Interceptor that was removed.
     */
    protected void notifyInterceptorRemoved(EnvelopeInterceptor interceptor){

        for (ChangeListener listener : listeners){

            listener.interceptorRemoved(interceptor);
        }
    }

    /**
     * Add a change listener to the provider.
     * @param listener Listener to add.
     */
    @Override
    public void addListener(ChangeListener listener) {

        checkNotNull(listener);

        listeners.add(listener);
    }

    /**
     * Remove a change listener from the provider.
     * @param listener Listener to remove.
     */
    @Override
    public void removeListener(ChangeListener listener) {

        checkNotNull(listener);

        listeners.remove(listener);
    }
}
