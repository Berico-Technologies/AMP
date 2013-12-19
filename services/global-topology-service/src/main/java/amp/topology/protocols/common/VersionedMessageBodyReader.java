package amp.topology.protocols.common;

import com.google.common.base.Optional;
import com.google.common.reflect.TypeToken;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * Extends the concept of the MessageBodyReader by supporting versioned message types
 * in an easy-to-extend way.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public abstract class VersionedMessageBodyReader<T> implements MessageBodyReader<T> {

    /**
     * What "T" represents to this instance.
     */
    TypeToken<T> TYPE_TOKEN = new TypeToken<T>(getClass()){};

    /**
     * Defines the signature of an adaptor that can handle the adaptation of a
     * version (or set of versions) for a particular type.
     *
     * @param <T> The type to convert to.
     */
    public interface VersionAdaptor<T> {

        /**
         * Can this version adaptor handle the specified version?
         * @param version Version to adapt
         * @return TRUE if it can, FALSE if it cannot.
         */
        boolean canAdapt(Versioned version);

        /**
         * Given the headers and the body of the request, adapt the message.
         * @param headers Headers
         * @param body Body
         * @return Instance of the adapted type.
         */
        T adapt(MultivaluedMap<String, String> headers, InputStream body);
    }

    /**
     * Get the latest adaptor (this is the default adaptor, used if no Versioned annotation is present).
     * @return Latest Adaptor.
     */
    protected abstract VersionAdaptor<T> getLatestAdaptor();

    /**
     * Get the set of available VersionAdaptors.
     * @return Set of Version Adaptors.
     */
    protected abstract Set<VersionAdaptor<T>> getAdaptors();

    /**
     * Select the most appropriate adaptor for the request.
     * @param versionedOptional Version, which is optional.
     * @return Either the latest adaptor, or the most appropriate for the version.
     */
    VersionAdaptor<T> selectMostAppropriateAdaptor(Optional<Versioned> versionedOptional){

        if (versionedOptional.isPresent()){

            Versioned versioned = versionedOptional.get();

            for (VersionAdaptor<T> adaptor : getAdaptors()){

                if (adaptor.canAdapt(versioned)) return adaptor;
            }
        }

        return getLatestAdaptor();
    }

    /**
     * This Message Body Reader can handle any serialized message representing an object
     * assignable from "T".
     * @param aClass
     * @param type
     * @param annotations
     * @param mediaType
     * @return
     */
    @Override
    public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {

        return TYPE_TOKEN.isAssignableFrom(type);
    }

    /**
     * Convert the message by delegating the task to the most applicable adaptor.
     * @param tClass
     * @param type
     * @param annotations
     * @param mediaType
     * @param headers Headers of the Request
     * @param body Body of the Request
     * @return
     * @throws IOException
     * @throws WebApplicationException
     */
    @Override
    public T readFrom(
            Class<T> tClass,
            Type type,
            Annotation[] annotations,
            MediaType mediaType,
            MultivaluedMap<String, String> headers,
            InputStream body) throws IOException, WebApplicationException {

        Optional<Versioned> version = Versioned.Helpers.find(annotations);

        VersionAdaptor<T> adaptor = selectMostAppropriateAdaptor(version);

        return adaptor.adapt(headers, body);
    }
}
