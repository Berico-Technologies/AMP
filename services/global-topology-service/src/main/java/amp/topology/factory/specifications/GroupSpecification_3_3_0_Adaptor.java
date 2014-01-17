package amp.topology.factory.specifications;

import amp.topology.factory.GroupSpecification;
import amp.topology.resources.common.JsonVersionedAdaptor;
import amp.topology.resources.common.Versioned;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class GroupSpecification_3_3_0_Adaptor extends JsonVersionedAdaptor<GroupSpecification> {

    @Override
    public boolean canAdapt(Versioned version) {

        return true;
    }
}
