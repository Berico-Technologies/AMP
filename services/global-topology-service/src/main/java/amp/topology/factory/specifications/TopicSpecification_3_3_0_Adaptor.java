package amp.topology.factory.specifications;

import amp.topology.factory.TopicSpecification;
import amp.topology.resources.common.JsonVersionedAdaptor;
import amp.topology.resources.common.Versioned;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class TopicSpecification_3_3_0_Adaptor extends JsonVersionedAdaptor<TopicSpecification> {

    @Override
    public boolean canAdapt(Versioned version) {
        return true;
    }
}
