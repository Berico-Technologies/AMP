package amp.topology.factory.specifications;

import amp.topology.resources.common.VersionedMessageBodyReader;
import amp.topology.factory.ConnectorSpecification;
import com.google.common.collect.Sets;

import javax.ws.rs.ext.Provider;
import java.util.Set;

/**
 * Converts the message body of a request into a ConnectorSpecification.
 *
 * @author Richard Clayton (Berico Technologies)
 */
@Provider
public class ConnectorSpecificationMessageBodyReader extends VersionedMessageBodyReader<ConnectorSpecification> {

    /**
     * List of delegate adaptors that can transform the message body into Connector Specification.
     */
    static final Set<VersionAdaptor<ConnectorSpecification>> ADAPTORS;

    /**
     * The Latest (and default) Adaptor.
     */
    static final VersionAdaptor<ConnectorSpecification> LATEST;

    /**
     * Initialize the Adaptors.
     */
    static {

        LATEST = new ConnectorSpecification_3_3_0_Adaptor();

        ADAPTORS = Sets.newCopyOnWriteArraySet();

        // Order by preference.  This means that if later versions of the
        // adaptor can handle earlier addition logic, allow them to handle
        // the request first, falling back to early version as needed.
        // The canAdapt() will determine whether an adaptor should be used.
        ADAPTORS.add(LATEST);
    }


    @Override
    protected VersionAdaptor<ConnectorSpecification> getLatestAdaptor() {

        return LATEST;
    }

    @Override
    protected Set<VersionAdaptor<ConnectorSpecification>> getAdaptors() {

        return ADAPTORS;
    }
}
