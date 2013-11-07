package amp.policy.core.impl;

import cmf.bus.EnvelopeHeaderConstants;

import java.util.HashMap;

/**
 * A helper for defining the criteria for intercepting a message.
 */
public class InterceptionCriteria extends HashMap<String, String> {

    public InterceptionCriteria setTopic(String type){

        put(EnvelopeHeaderConstants.MESSAGE_TOPIC, type);

        return this;
    }

    public InterceptionCriteria setSenderIdentity(String sender){

        put(EnvelopeHeaderConstants.MESSAGE_SENDER_IDENTITY, sender);

        return this;
    }
}
