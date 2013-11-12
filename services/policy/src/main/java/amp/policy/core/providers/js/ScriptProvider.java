package amp.policy.core.providers.js;

import amp.policy.core.adjudicators.javascript.ScriptConfiguration;

import java.util.Collection;

/**
 * Defines the needs of a service that provides scripts to the script interceptor.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public interface ScriptProvider {

    /**
     * Get all scripts known to the repository at the current moment.
     * @return Configuration of scripts.
     */
    Collection<ScriptConfiguration> get();

    /**
     * Add a change listener to the provider.
     * @param listener Listener to add.
     */
    void addChangeListener(ChangeListener listener);

    /**
     * Remove a change listener from the provider.
     * @param listener Listener to remove.
     */
    void removeChangeListener(ChangeListener listener);

    /**
     * Describes the notification interface for concerned services as to when a script is added or removed.
     */
    public interface ChangeListener {

        void scriptAdded(ScriptConfiguration configuration);

        void scriptRemoved(ScriptConfiguration configuration);
    }

}
