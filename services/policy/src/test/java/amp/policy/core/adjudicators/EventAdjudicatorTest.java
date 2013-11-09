package amp.policy.core.adjudicators;

import amp.policy.core.MockEvent;
import amp.policy.core.MockEventAdjudicator;
import amp.policy.core.PolicyEnforcer;
import amp.policy.core.SerializerFactory;
import amp.utility.serialization.ISerializer;
import cmf.bus.Envelope;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class EventAdjudicatorTest {

    private ISerializer createSerializer(MockEvent me){

        ISerializer serializer = mock(ISerializer.class);

        doReturn(me).when(serializer).byteDeserialize(any(byte[].class), eq(MockEvent.class));

        return serializer;
    }

    @Test
    public void the_correct_type_is_inferred_for_the_event(){

        MockEventAdjudicator adj = new MockEventAdjudicator();

        assertEquals(MockEvent.class.toString(), adj.getEventType());
    }

    @Test
    public void if_content_type_is_not_present_on_envelope_json_serializer_is_used(){

        SerializerFactory sf = mock(SerializerFactory.class);

        ISerializer xmlSerializer = mock(ISerializer.class);

        ISerializer jsonSerializer = mock(ISerializer.class);

        doReturn(xmlSerializer).when(sf).getByContentType("application/xml");

        doReturn(jsonSerializer).when(sf).getByContentType("application/json");

        Envelope jsonEvent = mock(Envelope.class);

        when(jsonEvent.getHeader(anyString())).thenReturn(null);

        assertFalse(jsonEvent.getHeaders().containsKey("Content-Type"));

        MockEventAdjudicator adj = spy(new MockEventAdjudicator());

        adj.setSerializerFactory(sf);

        PolicyEnforcer pe = mock(PolicyEnforcer.class);

        adj.adjudicate(jsonEvent, pe);

        verify(jsonSerializer).byteDeserialize(any(byte[].class), eq(MockEvent.class));
    }

    @Test
    public void event_adjudicator_correctly_approves_message(){

        SerializerFactory sf = mock(SerializerFactory.class);

        MockEvent mockEvent = new MockEvent();

        doReturn(createSerializer(mockEvent)).when(sf).getByContentType(anyString());

        MockEventAdjudicator adj = spy(new MockEventAdjudicator());

        adj.setSerializerFactory(sf);

        Envelope e = mock(Envelope.class);

        when(e.getHeader(anyString())).thenReturn("contentType");

        PolicyEnforcer pe = mock(PolicyEnforcer.class);

        adj.adjudicate(e, pe);

        verify(sf).getByContentType("contentType");

        verify(adj).adjudicate(mockEvent, e, pe);

        verify(pe).approve(e);
    }

    @Test
    public void event_adjudicator_correctly_rejects_message(){

        SerializerFactory sf = mock(SerializerFactory.class);

        MockEvent mockEvent = new MockEvent();

        mockEvent.rejectEvent = true;

        doReturn(createSerializer(mockEvent)).when(sf).getByContentType(anyString());

        MockEventAdjudicator adj = spy(new MockEventAdjudicator());

        adj.setSerializerFactory(sf);

        Envelope e = mock(Envelope.class);

        when(e.getHeader(anyString())).thenReturn("contentType");

        PolicyEnforcer pe = mock(PolicyEnforcer.class);

        adj.adjudicate(e, pe);

        verify(sf).getByContentType("contentType");

        verify(adj).adjudicate(mockEvent, e, pe);

        verify(pe).reject(eq(e), anyString());
    }

    @Test
    public void event_adjudicator_catches_exception_and_logs_the_error(){

        SerializerFactory sf = mock(SerializerFactory.class);

        MockEvent mockEvent = new MockEvent();

        mockEvent.throwException = true;

        doReturn(createSerializer(mockEvent)).when(sf).getByContentType(anyString());

        MockEventAdjudicator adj = spy(new MockEventAdjudicator());

        adj.setSerializerFactory(sf);

        Envelope e = mock(Envelope.class);

        when(e.getHeader(anyString())).thenReturn("contentType");

        PolicyEnforcer pe = mock(PolicyEnforcer.class);

        adj.adjudicate(e, pe);

        verify(sf).getByContentType("contentType");

        verify(adj).adjudicate(mockEvent, e, pe);

        verify(pe).log(eq(e), eq(PolicyEnforcer.LogTypes.ERROR), anyString());
    }

}
