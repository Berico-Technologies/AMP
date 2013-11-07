package amp.policy.core.adjudicators;

import amp.policy.core.EnvelopeAdjudicator;
import amp.policy.core.PolicyEnforcer;
import cmf.bus.Envelope;
import cmf.bus.EnvelopeHeaderConstants;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.HashMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Accept or Reject Messages by the sender identity
 */
public class AccessControlAdjudicator implements EnvelopeAdjudicator {

    private HashMap<String, AccessControlEntry> acl = Maps.newHashMap();

    private boolean banByDefault = true;

    public AccessControlAdjudicator(){}

    public AccessControlAdjudicator(Collection<AccessControlEntry> entries){

        setAccessControlList(entries);
    }

    public void setAccessControlList(Collection<AccessControlEntry> entries){

        checkNotNull(entries);

        for (AccessControlEntry entry : entries){

            if (!acl.containsKey(entry.getPrincipal())){

                acl.put(entry.getPrincipal(), entry);
            }
        }
    }

    public void setBanByDefault(boolean trueIfUsersNotInAccessControlListShouldBeBanned){

        banByDefault = trueIfUsersNotInAccessControlListShouldBeBanned;
    }

    @Override
    public void adjudicate(Envelope envelope, PolicyEnforcer enforcer) {

        try {

            Optional<String> senderOptional =
                    Optional.of(envelope.getHeader(EnvelopeHeaderConstants.MESSAGE_SENDER_IDENTITY));

            String sender = senderOptional.get();

            AccessControlEntry ace = acl.get(sender);

            if (ace == null){

                if (banByDefault)
                    enforcer.reject(envelope, "Explicit approval required for message.");
                else
                    enforcer.accept(envelope);

            }
            else {

                if (ace.getPermission() == AccessControlEntry.Permissions.APPROVED)
                    enforcer.accept(envelope);
                else
                    enforcer.reject(envelope, String.format("Sender %s is not allowed to send this message.", sender));
            }
        } catch (IllegalStateException ex){

            enforcer.reject(envelope, "No sender identity header property found on message.");
        }
    }
}
