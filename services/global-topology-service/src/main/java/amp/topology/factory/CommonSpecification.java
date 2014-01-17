package amp.topology.factory;

import amp.topology.anubis.AccessControlList;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public interface CommonSpecification {

    String getTopicId();

    @Nullable
    String getDescription();

    @Nullable
    AccessControlList getAccessControlList();

    @Nullable
    Map<String, Object> getConfigurationHints();
}
