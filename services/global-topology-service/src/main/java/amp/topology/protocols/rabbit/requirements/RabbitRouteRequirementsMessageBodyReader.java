package amp.topology.protocols.rabbit.requirements;

import amp.topology.protocols.common.VersionedMessageBodyReader;
import com.google.common.collect.Sets;

import javax.ws.rs.ext.Provider;
import java.util.Set;

/**
 * Transforms the body of a request into a RabbitRouteRequirements object.
 *
 * @author Richard Clayton (Berico Technologies)
 */
@Provider
public class RabbitRouteRequirementsMessageBodyReader extends VersionedMessageBodyReader<RabbitRouteRequirements> {

    /**
     * List of delegate adaptors that can transform the message body into Requirements.
     */
    static final Set<VersionAdaptor<RabbitRouteRequirements>> ADAPTORS;

    /**
     * The Latest (and default) Route Requirements Adaptor.
     */
    static final VersionAdaptor<RabbitRouteRequirements> LATEST;

    /**
     * Initialize the Adaptors.
     */
    static {

        LATEST = new RabbitRouteRequirements_3_3_0_Adaptor();

        ADAPTORS = Sets.newCopyOnWriteArraySet();

        // Order by preference.  This means that if later versions of the
        // adaptor can handle earlier addition logic, allow them to handle
        // the request first, falling back to early version as needed.
        // The canAdapt() will determine whether an adaptor should be used.
        ADAPTORS.add(LATEST);
    }


    @Override
    protected VersionAdaptor<RabbitRouteRequirements> getLatestAdaptor() {

        return LATEST;
    }

    @Override
    protected Set<VersionAdaptor<RabbitRouteRequirements>> getAdaptors() {

        return ADAPTORS;
    }
}
