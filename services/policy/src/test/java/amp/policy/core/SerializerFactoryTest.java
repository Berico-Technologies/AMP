package amp.policy.core;

import amp.utility.serialization.ISerializer;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class SerializerFactoryTest {

    private static final String CT_ATOM = "application/atom+xml";
    private static final String CT_JSON = "application/json";
    private static final String CT_SOAP = "application/soap+xml";

    @Test
    public void set_and_get_serializer_by_content_type(){

        Map<String, ISerializer> serializers = Maps.newHashMap();

        ISerializer expectedS1 = mock(ISerializer.class);

        ISerializer expectedS2 = mock(ISerializer.class);

        serializers.put(CT_ATOM, expectedS1);

        serializers.put(CT_JSON, expectedS2);

        SerializerFactory factory = new SerializerFactory(serializers);

        ISerializer actualS1 = factory.getByContentType(CT_ATOM);

        ISerializer actualS2 = factory.getByContentType(CT_JSON);

        assertEquals(expectedS1, actualS1);

        assertEquals(expectedS2, actualS2);

        ISerializer expectedS3 = mock(ISerializer.class);

        factory.addSerializer(CT_SOAP, expectedS3);

        ISerializer actualS3 = factory.getByContentType(CT_SOAP);

        assertEquals(expectedS3, actualS3);
    }
}
