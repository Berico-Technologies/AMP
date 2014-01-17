package amp.topology.resources.common;

import com.google.common.base.Optional;
import com.google.common.reflect.TypeToken;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

/**
 * @author Richard Clayton (Berico Technologies)
 */
@RunWith(MockitoJUnitRunner.class)
public class VersionedMessageBodyReaderTest
        extends VersionedMessageBodyReader<VersionedMessageBodyReaderTest.VersionedMessage> {


    @Test
    public void test_isReadable(){

        assertTrue(
                this.isReadable(
                        VersionedMessage.class,
                        new TypeToken<VersionedMessage>(){}.getType(),
                        VersionedMessage.class.getAnnotations(),
                        MediaType.APPLICATION_JSON_TYPE));

        assertFalse(
                this.isReadable(
                        String.class,
                        new TypeToken<String>(){}.getType(),
                        String.class.getAnnotations(),
                        MediaType.APPLICATION_JSON_TYPE));
    }

    @Test
    public void test_selectMostAppropriateAdaptor_by_specific_version(){

        Optional<Versioned> v1 = Versioned.Helpers.find(new VersionedMessage().getClass().getAnnotations());

        VersionAdaptor<VersionedMessage> actualV1 = this.selectMostAppropriateAdaptor(v1);

        assertEquals(V1, actualV1);

        Optional<Versioned> v2 = Versioned.Helpers.find(new VersionedMessageV2().getClass().getAnnotations());

        VersionAdaptor<VersionedMessage> actualV2 = this.selectMostAppropriateAdaptor(v2);

        assertEquals(V2, actualV2);

        Optional<Versioned> v3 = Versioned.Helpers.find(new VersionedMessageV3().getClass().getAnnotations());

        VersionAdaptor<VersionedMessage> actualV3 = this.selectMostAppropriateAdaptor(v3);

        assertEquals(LATEST_V3, actualV3);
    }

    @Test
    public void test_selectMostAppropriateAdaptor_with_unknown_version(){

        Optional<Versioned> v4 = Versioned.Helpers.find(new VersionedMessageV4().getClass().getAnnotations());

        VersionAdaptor<VersionedMessage> actualV4 = this.selectMostAppropriateAdaptor(v4);

        assertEquals(LATEST_V3, actualV4);
    }

    @Test
    public void test_selectMostAppropriateAdaptor_without_version_mention(){

        Optional<Versioned> latest = Versioned.Helpers.find(new VersionedMessageV5().getClass().getAnnotations());

        VersionAdaptor<VersionedMessage> actualLatest = this.selectMostAppropriateAdaptor(latest);

        assertEquals(LATEST_V3, actualLatest);
    }

    @Test
    public void test_readFrom_returns_anticipated_version() throws Exception {

        VersionedMessage actualV1 = readFrom(
                VersionedMessage.class,
                new TypeToken<VersionedMessage>(){}.getType(),
                VersionedMessage.class.getAnnotations(), // Should be the decider
                MediaType.APPLICATION_JSON_TYPE,
                null,
                new ByteArrayInputStream("".getBytes()));

        assertEquals(VersionedMessage.class, actualV1.getClass());

        VersionedMessage actualV2 = readFrom(
                VersionedMessage.class,
                new TypeToken<VersionedMessage>(){}.getType(),
                VersionedMessageV2.class.getAnnotations(), // Should be the decider
                MediaType.APPLICATION_JSON_TYPE,
                null,
                new ByteArrayInputStream("".getBytes()));

        assertEquals(VersionedMessageV2.class, actualV2.getClass());

        VersionedMessage actualV3 = readFrom(
                VersionedMessage.class,
                new TypeToken<VersionedMessage>(){}.getType(),
                VersionedMessageV3.class.getAnnotations(), // Should be the decider
                MediaType.APPLICATION_JSON_TYPE,
                null,
                new ByteArrayInputStream("".getBytes()));

        assertEquals(VersionedMessageV3.class, actualV3.getClass());
    }

    @Test
    public void test_readFrom_returns_last_adaptor_version() throws Exception {

        VersionedMessage actualV4 = readFrom(
                VersionedMessage.class,
                new TypeToken<VersionedMessage>(){}.getType(),
                VersionedMessageV4.class.getAnnotations(), // Should be the decider
                MediaType.APPLICATION_JSON_TYPE,
                null,
                new ByteArrayInputStream("".getBytes()));

        assertEquals(VersionedMessageV3.class, actualV4.getClass());

        VersionedMessage actualV5 = readFrom(
                VersionedMessage.class,
                new TypeToken<VersionedMessage>(){}.getType(),
                VersionedMessageV5.class.getAnnotations(), // Should be the decider
                MediaType.APPLICATION_JSON_TYPE,
                null,
                new ByteArrayInputStream("".getBytes()));

        assertEquals(VersionedMessageV3.class, actualV5.getClass());
    }

    @Mock
    public VersionAdaptor<VersionedMessage> LATEST_V3;

    @Mock
    public VersionAdaptor<VersionedMessage> V2;

    @Mock
    public VersionAdaptor<VersionedMessage> V1;

    Set<VersionAdaptor<VersionedMessage>> adaptors;

    @Before public void initialize(){


        when(LATEST_V3.canAdapt(any(Versioned.class))).thenAnswer(new AdaptAnswer("LATEST", "V3"));

        when(LATEST_V3.adapt(any(MultivaluedMap.class), any(InputStream.class))).thenReturn(new VersionedMessageV3());

        when(V1.canAdapt(any(Versioned.class))).thenAnswer(new AdaptAnswer("V1"));

        when(V1.adapt(any(MultivaluedMap.class), any(InputStream.class))).thenReturn(new VersionedMessage());

        when(V2.canAdapt(any(Versioned.class))).thenAnswer(new AdaptAnswer("V2"));

        when(V2.adapt(any(MultivaluedMap.class), any(InputStream.class))).thenReturn(new VersionedMessageV2());

        adaptors = new CopyOnWriteArraySet<VersionAdaptor<VersionedMessage>>(Arrays.asList(LATEST_V3, V1, V2));
    }

    @Override
    protected VersionAdaptor<VersionedMessage> getLatestAdaptor() {

        return LATEST_V3;
    }

    @Override
    protected Set<VersionAdaptor<VersionedMessage>> getAdaptors() {

        return adaptors;
    }

    static class AdaptAnswer implements  Answer<Boolean> {

        final String[] handlableVersions;

        AdaptAnswer(String... handlableVersions) {

            this.handlableVersions = handlableVersions;
        }

        @Override
        public Boolean answer(InvocationOnMock invocationOnMock) throws Throwable {

            Versioned v = (Versioned)invocationOnMock.getArguments()[0];

            for (String handlableVersion : handlableVersions)
                if (handlableVersion.equals(v.value())) return true;

            return false;
        }
    }

    @Versioned("V1")
    public static class VersionedMessage {}

    @Versioned("V2")
    public static class VersionedMessageV2 extends VersionedMessage {}

    @Versioned("V3")
    public static class VersionedMessageV3 extends VersionedMessage {}

    @Versioned("V4")
    public static class VersionedMessageV4 extends VersionedMessage {}

    @Versioned
    public static class VersionedMessageV5 extends VersionedMessage {}
}
