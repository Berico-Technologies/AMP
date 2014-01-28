package amp.topology.global.impl;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class TestPartition extends BasePartition {

    public static final Map<String, String> EXTENSIONS;

    static {
        EXTENSIONS = Maps.newHashMap();
        EXTENSIONS.put("key1", "value1");
        EXTENSIONS.put("key2", "value2");
    }

    @Override
    public void setup() throws Exception {}

    @Override
    public void cleanup() throws Exception {}

    @Override
    public Map<String, String> getExtensionProperties() {

        return EXTENSIONS;
    }

    @Override
    public void verify() throws Exception {}

    @Override
    public void set(Map<String, String> properties) {}

    public void changeState(PartitionStates state, String reason){
        setState(state, reason);
    }
}
