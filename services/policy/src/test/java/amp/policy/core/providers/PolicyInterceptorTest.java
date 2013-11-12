package amp.policy.core.providers;

import amp.policy.core.Enforcer;
import amp.policy.core.EnvelopeAdjudicator;
import amp.policy.core.EnvelopeInterceptor;
import cmf.bus.EnvelopeHeaderConstants;
import com.google.common.base.Strings;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class PolicyInterceptorTest {

    protected EnvelopeInterceptor createInteceptor(EnvelopeAdjudicator adj){

        Enforcer enforcer = mock(Enforcer.class);

        return PolicyInterceptor.Helper.createInterceptor(adj, enforcer);
    }


    @Test
    public void default_values_return_sensibly(){

        EnvelopeInterceptor actual = createInteceptor(new MockAdjudicator1());

        assertFalse(Strings.isNullOrEmpty(actual.getId()));
        assertNotEquals(PolicyInterceptor.DEFAULT_VALUE, actual.getId());

        assertEquals(MockAdjudicator1.class.getCanonicalName(), actual.getDescription());

        assertEquals(1, actual.getRegistrationInfo().size());

        assertEquals("amp.policy.Mock1", actual.getRegistrationInfo().get(EnvelopeHeaderConstants.MESSAGE_TOPIC));
    }

    @Test
    public void attribute_information_is_correctly_parsed(){

        EnvelopeInterceptor actual = createInteceptor(new MockAdjudicator2());

        assertEquals("abc123", actual.getId());

        assertEquals("abc123", actual.getDescription());

        assertEquals(5, actual.getRegistrationInfo().size());

        assertEquals("1", actual.getRegistrationInfo().get("a"));
        assertEquals("2", actual.getRegistrationInfo().get("b"));
        assertEquals("3", actual.getRegistrationInfo().get("c"));
        assertEquals("amp.policy.Mock2", actual.getRegistrationInfo().get(EnvelopeHeaderConstants.MESSAGE_TOPIC));
        assertEquals("jdoe", actual.getRegistrationInfo().get(EnvelopeHeaderConstants.MESSAGE_SENDER_IDENTITY));
    }
}
