package amp.topology.global.anubis;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class AccessControlList {

    private ConcurrentMap<String, AccessControl> accessControlList = Maps.newConcurrentMap();

    public AccessControlList(){}

    public AccessControlList(Map<String, AccessControl> accessControlList) {

        setAccessControlList(accessControlList);
    }

    public void setAccessControlList(Map<String, AccessControl> accessControlList) {

        if (accessControlList != null && accessControlList.size() > 0){

            this.accessControlList.putAll(accessControlList);
        }
    }

    public AccessControl getAccessControl(String operation){

        return accessControlList.get(operation);
    }

    public void addAccessControl(String operation, AccessControl accessControl){

        this.accessControlList.put(operation, accessControl);
    }

    public boolean removeAccessControl(String operation){

        return this.accessControlList.remove(operation) != null;
    }
}
