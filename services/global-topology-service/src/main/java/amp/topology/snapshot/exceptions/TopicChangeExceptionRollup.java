package amp.topology.snapshot.exceptions;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * An aggregator of exceptions encountered during a topology synchronization.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class TopicChangeExceptionRollup extends Exception {

    private final ArrayList<TopicChangeException> rollup = Lists.newArrayList();

    /**
     * Register a failure that occurred for a topic.  You may register multiple failures
     * for the same topic.
     * @param topicId BasicTopic that failed to change.
     * @param cause Cause of the failure.
     */
    public void registerFailure(String topicId, Throwable cause){

        this.rollup.add(new TopicChangeException(topicId, cause));
    }

    /**
     * Get the rollup (as unmodifiable collection).
     * @return rollup of errors encountered.
     */
    public Collection<TopicChangeException> getRollup(){

        return Collections.unmodifiableCollection(rollup);
    }

    /**
     * Has an error been registered?
     * @return TRUE if at least one error has occurred.
     */
    public boolean hasErrors(){

        return rollup.size() > 0;
    }

    /**
     * Represents an exception wrapped with the TopicConfiguration context.
     */
    public static class TopicChangeException extends Exception {

        public TopicChangeException(String topicConfigurationId, Throwable cause) {

            super(
                    String.format(
                            "BasicTopic '%s' could not be modified: %s.",
                            topicConfigurationId,
                            cause.getMessage()),
                    cause);
        }
    }
}
