package amp.topology.resources.common;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import javax.ws.rs.core.MultivaluedMap;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

/**
 * Assumes you have a JSON serialized form of your expected object.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public abstract class JsonVersionedAdaptor<T> implements VersionedMessageBodyReader.VersionAdaptor<T> {

    Type type;

    Gson gson = getGsonInstance();

    /**
     * You may override this and configure the Gson instance as you see fit.
     * @return Gson instance.
     */
    protected Gson getGsonInstance(){

        return new Gson();
    }

    /**
     * Get the type of 'T'.
     * @return
     */
    protected Type getType(){

        if (this.type == null)
            type = new TypeToken<T>(getClass()){}.getType();

        return this.type;
    }

    /**
     * Adapt the body of the request into a Java object.
     * @param headers Headers Headers of the request.
     * @param body Body Body (json) of the request.
     * @return An instance of T.
     */
    @Override
    public T adapt(MultivaluedMap<String, String> headers, InputStream body) {

        InputStreamReader reader = new InputStreamReader(body);

        return gson.fromJson(reader, getType());
    }
}
