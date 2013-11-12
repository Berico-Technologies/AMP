package amp.policy.core.adjudicators;


import amp.bus.EnvelopeHelper;
import amp.policy.core.Enforcer;
import amp.policy.core.adjudicators.security.AccessControlEntry;
import cmf.bus.Envelope;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class AccessControlAdjudicatorTest {

    private Envelope createEnvelopeWithSender(String sender){

        return new EnvelopeHelper()
                .setMessageTopic("blah")
                .setSenderIdentity(sender)
                .setPayload("{ \"hello\": \"policy\"}".getBytes())
                .getEnvelope();
    }


    private List<AccessControlEntry> createTestAccessControlList(){
        return Lists.newArrayList(
                new AccessControlEntry("jruiz"),
                new AccessControlEntry("rclayton"),
                new AccessControlEntry(AccessControlEntry.Permissions.DENIED, "tim")
        );
    }

    @Test
    public void adjudicate_correctly_enforces_policy(){

        List<AccessControlEntry> acl = createTestAccessControlList();

        AccessControlAdjudicator adjudicator = new AccessControlAdjudicator(acl);

        Enforcer enforcer = mock(Enforcer.class);

        Envelope e1 = createEnvelopeWithSender("rclayton");

        adjudicator.adjudicate(e1, enforcer);

        verify(enforcer).approve(e1);
        verify(enforcer, never()).reject(eq(e1), anyString());

        Envelope e2 = createEnvelopeWithSender("jruiz");

        adjudicator.adjudicate(e2, enforcer);

        verify(enforcer).approve(e2);
        verify(enforcer, never()).reject(eq(e2), anyString());

        Envelope e3 = createEnvelopeWithSender("tim");

        adjudicator.adjudicate(e3, enforcer);

        verify(enforcer, never()).approve(e3);
        verify(enforcer).reject(eq(e3), anyString());

    }

    @Test
    public void users_are_implicitly_denied_if_not_in_access_control_list(){

        List<AccessControlEntry> acl = createTestAccessControlList();

        AccessControlAdjudicator adjudicator = new AccessControlAdjudicator(acl);

        Enforcer enforcer = mock(Enforcer.class);

        Envelope shouldBeDenied = createEnvelopeWithSender("jsmith");

        adjudicator.adjudicate(shouldBeDenied, enforcer);

        verify(enforcer, never()).approve(shouldBeDenied);
        verify(enforcer).reject(eq(shouldBeDenied), anyString());
    }

    @Test
    public void when_not_banning_by_default_unknown_users_should_have_messages_forwarded(){

        List<AccessControlEntry> acl = createTestAccessControlList();

        AccessControlAdjudicator adjudicator = new AccessControlAdjudicator(acl);

        adjudicator.setBanByDefault(false);

        Enforcer enforcer = mock(Enforcer.class);

        Envelope shouldBeApproved = createEnvelopeWithSender("jsmith");

        adjudicator.adjudicate(shouldBeApproved, enforcer);

        verify(enforcer).approve(shouldBeApproved);
        verify(enforcer, never()).reject(eq(shouldBeApproved), anyString());
    }

}
