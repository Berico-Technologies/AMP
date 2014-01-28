package amp.topology.factory.specifications;

import amp.topology.resources.common.VersionedMessageBodyReader;
import amp.topology.factory.TopicSpecification;
import com.google.common.collect.Sets;

import javax.ws.rs.ext.Provider;
import java.util.Set;

/**
 * Converts the message body of a request into a TopicSpecification.
 *
 * @author Richard Clayton (Berico Technologies)
 */
@Provider
public class TopicSpecificationMessageBodyReader extends VersionedMessageBodyReader<TopicSpecification> {

    /**
     * List of delegate adaptors that can transform the message body into BaseTopic Specification.
     */
    static final Set<VersionAdaptor<TopicSpecification>> ADAPTORS;

    /**
     * The Latest (and default) Adaptor.
     */
    static final VersionAdaptor<TopicSpecification> LATEST;

    /**
     * Initialize the Adaptors.
     */
    static {

        LATEST = new TopicSpecification_3_3_0_Adaptor();

        ADAPTORS = Sets.newCopyOnWriteArraySet();

        // Order by preference.  This means that if later versions of the
        // adaptor can handle earlier addition logic, allow them to handle
        // the request first, falling back to early version as needed.
        // The canAdapt() will determine whether an adaptor should be used.
        ADAPTORS.add(LATEST);
    }


    @Override
    protected VersionAdaptor<TopicSpecification> getLatestAdaptor() {

        return LATEST;
    }

    @Override
    protected Set<VersionAdaptor<TopicSpecification>> getAdaptors() {

        return ADAPTORS;
    }
}
