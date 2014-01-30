package amp.topology.factory;

import java.util.ArrayList;

/**
 * A simple log of modifications that may have occurred when attempting to
 * mutate a BasicTopic, BaseGroup, or BaseConnector.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class Modifications extends ArrayList<Modifications.Entry> {

    /**
     * Represents an individual modification, and whether it was successful or not.
     */
    public static class Entry {

        private final String propertyName;

        private final boolean wasChanged;

        private final Object oldValue;

        private final Object newValue;

        public Entry(String propertyName, boolean wasChanged, Object oldValue, Object newValue) {
            this.propertyName = propertyName;
            this.wasChanged = wasChanged;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public boolean isWasChanged() {
            return wasChanged;
        }

        public Object getOldValue() {
            return oldValue;
        }

        public Object getNewValue() {
            return newValue;
        }
    }

}
