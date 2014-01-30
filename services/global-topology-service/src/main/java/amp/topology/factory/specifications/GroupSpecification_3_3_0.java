package amp.topology.factory.specifications;

import amp.topology.anubis.AccessControlList;
import amp.topology.factory.GroupSpecification;

import java.util.Map;

/**
 * Specification for creating Groups via the GroupFactory.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class GroupSpecification_3_3_0 extends CommonSpecification_3_3_0 implements GroupSpecification {

    private String groupId;

    private boolean isConsumerGroup;

    private String protocol;

    protected GroupSpecification_3_3_0(){}

    /**
     * Initialize with the required parameters for specifying a BaseGroup.
     *
     * @param topicId The id of the topic in which this connector should exist.
     * @param description A description of the purpose of the group.
     * @param accessControlList Access controls for the group (outside of the defaults).
     * @param configurationHints Any hints to the factory about how to create or configure the group.  This may
     *                           include something about the desired protocol, producing configuration, whatever.
     *                           The key-value pairs will be specific to the underlying implementation of the
     *                           TopicFactory and any delegated factories it may use.
     * @param groupId The desired id of the group, must be unique within the topic.
     * @param isConsumerGroup Is this a Consumer BaseGroup? TRUE = Consumer, FALSE = Producer
     * @param protocol The primary protocol of this BaseGroup.
     */
    public GroupSpecification_3_3_0(
            String topicId,
            String description,
            AccessControlList accessControlList,
            Map<String, Object> configurationHints,
            String groupId,
            boolean isConsumerGroup,
            String protocol) {

        super(topicId, description, accessControlList, configurationHints);
        this.groupId = groupId;
        this.isConsumerGroup = isConsumerGroup;
        this.protocol = protocol;
    }

    /**
     * The desired Id of the group.  Must be unique within the topic is belongs in.
     * @return The desired id of the group.
     */
    @Override
    public String getGroupId() {
        return groupId;
    }

    /**
     * Is this a Consumer BaseGroup?
     * @return TRUE if it is, FALSE if it's a Producer BaseGroup.
     */
    @Override
    public boolean isConsumerGroup() {
        return isConsumerGroup;
    }

    /**
     * Get the primary protocol of this BaseGroup.
     * @return Primary protocol
     */
    @Override
    public String getProtocol() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Get a builder for the GroupSpecification_3_3_0.
     * @return BaseGroup Specification Builder.
     */
    public GroupSpecificationBuilder builder(){

        return new GroupSpecificationBuilder();
    }

    /**
     * A Builder for the GroupSpecification_3_3_0 class.
     */
    public static class GroupSpecificationBuilder
            extends CommonSpecificationBuilder<GroupSpecification_3_3_0, GroupSpecificationBuilder> {

        protected GroupSpecificationBuilder() {  super(new GroupSpecification_3_3_0()); }

        /**
         * The id of the group, which must be unique within a BasicTopic.
         * @param id ID of the group.
         * @return this.
         */
        public GroupSpecificationBuilder groupId(String id){

            this.objectUnderConstruction.groupId = id;

            return self();
        }

        /**
         * Is this a Consumer BaseGroup?
         * @param isConsumer TRUE if it is, FALSE if it is a Producer BaseGroup.
         * @return this.
         */
        public GroupSpecificationBuilder isConsumer(boolean isConsumer){

            this.objectUnderConstruction.isConsumerGroup = isConsumer;

            return self();
        }

        /**
         * Set that this is a Consumer BaseGroup.
         * @return this.
         */
        public GroupSpecificationBuilder consumer(){

            this.objectUnderConstruction.isConsumerGroup = true;

            return self();
        }

        /**
         * Set that this is a Producer BaseGroup.
         * @return this.
         */
        public GroupSpecificationBuilder producer(){

            this.objectUnderConstruction.isConsumerGroup = false;

            return self();
        }

        /**
         * Set the primary protocol of the BaseGroup.
         * @param protocol
         * @return this.
         */
        public GroupSpecificationBuilder protocol(String protocol){

            this.objectUnderConstruction.protocol = protocol;

            return self();
        }
    }
}
