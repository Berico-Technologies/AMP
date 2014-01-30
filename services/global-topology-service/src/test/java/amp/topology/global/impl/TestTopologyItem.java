package amp.topology.global.impl;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class TestTopologyItem extends BaseTopologyItem<TopologyState> {

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
    public TopologyState dehydrate() { return null; }

    @Override
    public Map<String, String> getExtensionProperties() { return EXTENSIONS; }

    @Override
    public void save() {}

    @Override
    public void save(boolean saveAggregates) {}

    @Override
    public void set(Map<String, String> properties) {}
}
