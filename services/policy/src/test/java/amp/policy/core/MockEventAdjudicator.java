package amp.policy.core;

import amp.policy.core.adjudicators.EventAdjudicator;
import cmf.bus.Envelope;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class MockEventAdjudicator extends EventAdjudicator<MockEvent> {


    @Override
    public void adjudicate(MockEvent event, Envelope envelope, Enforcer enforcer) {

        if (event.throwException){

            throw new RuntimeException("Simulating unchecked exception.");
        }
        else if (event.rejectEvent) {

            enforcer.reject(envelope, "Instructed by event to reject.");
        }
        else {

            enforcer.approve(envelope);
        }
    }
}
