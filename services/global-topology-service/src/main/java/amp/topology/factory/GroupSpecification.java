package amp.topology.factory;

import amp.topology.anubis.AccessControlList;

import java.util.Map;

/**
 * Specification for creating Groups via the TopicFactory.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class GroupSpecification extends CommonSpecification {

    private String groupId;

    private boolean isConsumerGroup;

    protected GroupSpecification(){}

    /**
     * Initialize with the required parameters for specifying a Group.
     *
     * @param topicId The id of the topic in which this connector should exist.
     * @param description A description of the purpose of the group.
     * @param accessControlList Access controls for the group (outside of the defaults).
     * @param configurationHints Any hints to the factory about how to create or configure the group.  This may
     *                           include something about the desired protocol, producing configuration, whatever.
     *                           The key-value pairs will be specific to the underlying implementation of the
     *                           TopicFactory and any delegated factories it may use.
     * @param groupId The desired id of the group, must be unique within the topic.
     * @param isConsumerGroup Is this a Consumer Group? TRUE = Consumer, FALSE = Producer
     */
    public GroupSpecification(
            String topicId,
            String description,
            AccessControlList accessControlList,
            Map<String, Object> configurationHints,
            String groupId,
            boolean isConsumerGroup) {

        super(topicId, description, accessControlList, configurationHints);
        this.groupId = groupId;
        this.isConsumerGroup = isConsumerGroup;
    }

    /**
     * The desired Id of the group.  Must be unique within the topic is belongs in.
     * @return The desired id of the group.
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * Is this a Consumer Group?
     * @return TRUE if it is, FALSE if it's a Producer Group.
     */
    public boolean isConsumerGroup() {
        return isConsumerGroup;
    }

    /**
     * Get a builder for the GroupSpecification.
     * @return Group Specification Builder.
     */
    public GroupSpecificationBuilder builder(){

        return new GroupSpecificationBuilder();
    }

    /**
     * A Builder for the GroupSpecification class.
     */
    public static class GroupSpecificationBuilder
            extends CommonSpecificationBuilder<GroupSpecification, GroupSpecificationBuilder> {

        protected GroupSpecificationBuilder() {  super(new GroupSpecification()); }

        /**
         * The id of the group, which must be unique within a Topic.
         * @param id ID of the group.
         * @return this.
         */
        public GroupSpecificationBuilder groupId(String id){

            this.objectUnderConstruction.groupId = id;

            return self();
        }

        /**
         * Is this a Consumer Group?
         * @param isConsumer TRUE if it is, FALSE if it is a Producer Group.
         * @return this.
         */
        public GroupSpecificationBuilder isConsumer(boolean isConsumer){

            this.objectUnderConstruction.isConsumerGroup = isConsumer;

            return self();
        }

        /**
         * Set that this is a Consumer Group.
         * @return this.
         */
        public GroupSpecificationBuilder consumer(){

            this.objectUnderConstruction.isConsumerGroup = true;

            return self();
        }

        /**
         * Set that this is a Producer Group.
         * @return this.
         */
        public GroupSpecificationBuilder producer(){

            this.objectUnderConstruction.isConsumerGroup = false;

            return self();
        }
    }
}
