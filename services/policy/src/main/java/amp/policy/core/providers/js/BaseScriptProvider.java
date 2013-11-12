package amp.policy.core.providers.js;

import amp.policy.core.adjudicators.javascript.ScriptConfiguration;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Handles all the listener boilerplate.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public abstract class BaseScriptProvider implements ScriptProvider {

    ArrayList<ChangeListener> listeners = Lists.newArrayList();

    protected void fireScriptAdded(ScriptConfiguration script){

        for (ChangeListener listener : listeners){

            listener.scriptAdded(script);
        }
    }

    protected void fireScriptRemoved(ScriptConfiguration script){

        for (ChangeListener listener : listeners){

            listener.scriptRemoved(script);
        }
    }

    @Override
    public void addChangeListener(ChangeListener listener) {

        checkNotNull(listener);

        this.listeners.add(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {

        checkNotNull(listener);

        this.listeners.remove(listener);
    }
}
