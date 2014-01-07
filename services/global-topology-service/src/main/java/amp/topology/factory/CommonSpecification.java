package amp.topology.factory;

import amp.topology.anubis.AccessControlList;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Common behavior shared by specification implementations.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public abstract class CommonSpecification {

    protected String topicId;

    protected String description;

    protected AccessControlList accessControlList;

    protected Map<String, Object> configurationHints;

    protected CommonSpecification(){}

    /**
     *
     * @param topicId Desired ID of the topic.  This is typically what you would refer to the Topic as.
     * @param description (optional) A description of the purpose of the topology item.
     * @param accessControlList (optional) Access controls for the item (outside of the defaults).
     * @param configurationHints (optional) Any hints to the factory about how to create or configure the item.
     *                           This may include something about the desired protocol, producing configuration,
     *                           whatever. The key-value pairs will be specific to the underlying implementation of
     *                           the TopicFactory and any delegated factories it may use.
     */
    public CommonSpecification(
            String topicId,
            @Nullable String description,
            @Nullable AccessControlList accessControlList,
            @Nullable Map<String, Object> configurationHints) {

        this.topicId = topicId;
        this.description = description;
        this.accessControlList = accessControlList;
        this.configurationHints = configurationHints;
    }

    /**
     * ID of the Topic.  This must be unique within the Topic space. Typically, this corresponds
     * to the type of event being produced or consumed (e.g. amp.events.UserPrivilegesRevokedEvent).
     * @return Topic ID.
     */
    public String getTopicId() {
        return topicId;
    }

    /**
     * A description of the purpose of the item.
     * @return Friendly description of how this thing is used.
     */
    public @Nullable String getDescription() {
        return description;
    }

    /**
     * Access Control List for this item.
     * @return ACL.
     */
    public @Nullable AccessControlList getAccessControlList() {
        return accessControlList;
    }

    /**
     * Hints used by the Factory to determine how this item should be created.  If NULL,
     * the defaults are generally assumed.
     * @return Configuration Hints.
     */
    public @Nullable Map<String, Object> getConfigurationHints() {
        return configurationHints;
    }


    /**
     * The base implementation for Specification builders.
     *
     * @param <T> Type of object being constructed.
     * @param <B> Type of the Builder performing the construction.
     */
    public static class CommonSpecificationBuilder<T extends CommonSpecification, B extends CommonSpecificationBuilder<T, B>> {

        protected T objectUnderConstruction;

        /**
         * Ghetto fix for type erasure encounter by extending classes.  Use this method to ensure that
         * the correct type (extending class type) is returned, and not the base builder class.
         * @return
         */
        @SuppressWarnings("unchecked")
        protected B self(){ return (B)this; }

        /**
         * Instantiate the class with the object that should be mutated.
         * @param objectUnderConstruction Object to be mutated.
         */
        protected CommonSpecificationBuilder(T objectUnderConstruction){

            this.objectUnderConstruction = objectUnderConstruction;
            this.objectUnderConstruction.configurationHints = Maps.newHashMap();
        }

        /**
         * Set the ID of the topic.
         * @param id ID of the topic.
         * @return this.
         */
        public B topic(String id){

            objectUnderConstruction.topicId = id;

            return self();
        }

        /**
         * Set the description of the item.
         * @param description Description of the item.
         * @return this.
         */
        public B description(String description){

            objectUnderConstruction.description = description;

            return self();
        }

        /**
         * Set the ACL of the item.
         * @param accessControlList ACL
         * @return this.
         */
        public B acl(AccessControlList accessControlList){

            objectUnderConstruction.accessControlList = accessControlList;

            return self();
        }

        /**
         * Add a configuration hint to the item.
         * @param key Key of the Hint.
         * @param value Value of the Hint.
         * @return this.
         */
        public B hint(String key, Object value){

            objectUnderConstruction.configurationHints.put(key, value);

            return self();
        }

        /**
         * Build the object.
         * @return the object under construction.
         */
        public T build(){

            return objectUnderConstruction;
        }
    }
}
