package amp.topology.factory.specifications;

import amp.topology.anubis.AccessControlList;
import amp.topology.factory.TopicSpecification;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Specification for creating Topics via the TopicFactory.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class TopicSpecification_3_3_0 extends CommonSpecification_3_3_0 implements TopicSpecification {

    boolean createDefaults = true;

    protected TopicSpecification_3_3_0(){}

    /**
     * Initialize with the required parameters for specifying a BasicTopic.
     * @param topicId Desired ID of the topic.  This is typically what you would refer to the BasicTopic as.
     * @param description A description of the purpose of the topic.
     * @param accessControlList Access controls for the topic (outside of the defaults).
     * @param configurationHints Any hints to the factory about how to create or configure the topic.  This may
     *                           include something about the desired protocol, producing configuration, whatever.
     *                           The key-value pairs will be specific to the underlying implementation of the
     *                           TopicFactory and any delegated factories it may use.
     * @param createDefaults Should any of the default topology or configuration be applied to this BasicTopic.  If
     *                       FALSE, the request is expected to configure the topic in subsequent calls.
     */
    public TopicSpecification_3_3_0(
            String topicId,
            String description,
            @Nullable AccessControlList accessControlList,
            @Nullable Map<String, Object> configurationHints,
            boolean createDefaults) {

        super(topicId, description, accessControlList, configurationHints);

        this.createDefaults = createDefaults;
    }

    /**
     * If the BasicTopic Configuration supports a default route set (or something else), should that route set be created?
     * @return TRUE if the default topology constructs should be created.
     */
    @Override
    public boolean shouldCreateDefaults() {
        return createDefaults;
    }

    public TopicSpecificationBuilder builder(){

        return new TopicSpecificationBuilder();
    }

    /**
     * A Builder for the TopicSpecification_3_3_0 class.
     */
    public static class TopicSpecificationBuilder
            extends CommonSpecificationBuilder<TopicSpecification_3_3_0, TopicSpecificationBuilder> {

        protected TopicSpecificationBuilder() {  super(new TopicSpecification_3_3_0()); }

        /**
         * Set whether the default topology items and configuration should be add.
         * @param yesTrue TRUE if defaults should be added.
         * @return this.
         */
        public TopicSpecificationBuilder defaults(boolean yesTrue){

            this.objectUnderConstruction.createDefaults = yesTrue;

            return self();
        }
    }
}
