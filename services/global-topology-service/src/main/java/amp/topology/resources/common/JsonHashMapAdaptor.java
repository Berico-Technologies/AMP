package amp.topology.resources.common;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import javax.ws.rs.core.MultivaluedMap;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * An adaptor implementation where the expected payload is a Map (HashMap for a concrete instance),
 * that will be adapted to the correct object.
 *
 * Converting Map<String, String> is a common technique employed in AMPere to allow implementations
 * to accept parameters not defined in the core API's (essentially extensions).
 *
 * @author Richard Clayton (Berico Technologies)
 */
public abstract class JsonHashMapAdaptor<T> implements VersionedMessageBodyReader.VersionAdaptor<T> {

    private static final Type HASHMAP_S_S_TYPE_TOKEN = new TypeToken<Map<String, String>>(){}.getType();

    Gson gson = getGsonInstance();

    /**
     * You may override this and configure the Gson instance as you see fit.
     * @return Gson instance.
     */
    protected Gson getGsonInstance(){

        return new Gson();
    }

    /**
     * Most derivations only need to implement this.  Given a HashMap<String, String>, return the
     * actual object of type T that you are expecting.
     * @param headers Headers of the request, if needed.
     * @param map The property bag to convert to a T.
     * @return an instance of T.
     */
    protected abstract T convertFromMap(MultivaluedMap<String, String> headers, HashMap<String, String> map);

    /**
     * Adapt the request
     * @param headers Headers Headers of the request.
     * @param body Body body (Json Map<String, String>) of the request.
     * @return An instance of T.
     */
    @Override
    public T adapt(MultivaluedMap<String, String> headers, InputStream body) {

        InputStreamReader reader = new InputStreamReader(body);

        HashMap<String, String> map = gson.fromJson(reader, HASHMAP_S_S_TYPE_TOKEN);

        return convertFromMap(headers, map);
    }

}
