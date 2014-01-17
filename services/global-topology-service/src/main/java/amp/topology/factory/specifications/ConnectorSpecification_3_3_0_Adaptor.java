package amp.topology.factory.specifications;

import amp.topology.factory.ConnectorSpecification;
import amp.topology.resources.common.JsonVersionedAdaptor;
import amp.topology.resources.common.Versioned;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class ConnectorSpecification_3_3_0_Adaptor extends JsonVersionedAdaptor<ConnectorSpecification> {

    @Override
    public boolean canAdapt(Versioned version) {

        return true;
    }
}
