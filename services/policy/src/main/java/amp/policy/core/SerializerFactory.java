package amp.policy.core;


import amp.utility.serialization.ISerializer;
import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for the serializers needed to transform message payloads into objects.
 */
public class SerializerFactory {

    private HashMap<String, ISerializer> serializers = Maps.newHashMap();

    public SerializerFactory(){}

    public SerializerFactory(Map<String, ISerializer> serializers){

        setSerializers(serializers);
    }

    public void setSerializers(Map<String, ISerializer> serializers){

        this.serializers.putAll(serializers);
    }

    public void addSerializer(String contentType, ISerializer serializer){

        if (!serializers.containsKey(contentType)){

            this.serializers.put(contentType, serializer);
        }
    }

    public ISerializer getByContentType(String contentType){

        return this.serializers.get(contentType);
    }
}
