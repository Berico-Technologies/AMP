package amp.policy.core.providers;

import amp.policy.core.EnvelopeInterceptor;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class MockInterceptorProvider extends BaseInterceptorProvider {

    public void addEnvelopeInterceptor(EnvelopeInterceptor ei){

        this.notifyInterceptorAdded(ei);
    }

    public void removeEnvelopeInterceptor(EnvelopeInterceptor ei){

        this.notifyInterceptorRemoved(ei);
    }

    public List<ChangeListener> getListeners(){ return this.listeners; }

    @Override
    public Collection<EnvelopeInterceptor> get() { return Lists.newArrayList(); }
}
