package amp.policy.core;

import cmf.bus.Envelope;
import cmf.bus.IEnvelopeFilterPredicate;
import cmf.bus.IRegistration;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 *  A registration that binds a set of envelopes to a particular policy adjudicator.
 */
public class EnvelopeInterceptor implements IRegistration {

    private static final Logger LOG = LoggerFactory.getLogger(EnvelopeInterceptor.class);

    private String id;

    private String description;

    private EnvelopeAdjudicator adjudicator;

    private PolicyEnforcer enforcer;

    private final Predicate<Envelope> filterPredicate;

    private HashMap<String, String> registrationInfo = Maps.newHashMap();

    public EnvelopeInterceptor(
            String id,
            String description,
            EnvelopeAdjudicator adjudicator,
            PolicyEnforcer enforcer,
            Map<String, String> registrationInfo){

        this(id, description, adjudicator, enforcer, new AdjudicateAllPredicate(), registrationInfo);
    }

    public EnvelopeInterceptor(
            String id,
            String description,
            EnvelopeAdjudicator adjudicator,
            PolicyEnforcer enforcer,
            Predicate<Envelope> filterPredicate,
            Map<String, String> registrationInfo){

        this.id = id;
        this.description = description;
        this.adjudicator = adjudicator;
        this.enforcer = enforcer;
        this.filterPredicate = filterPredicate;
        this.registrationInfo.putAll(registrationInfo);

    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public String getAdjudicatorType(){
        return adjudicator.getClass().toString();
    }

    public String getEnforcerType(){
        return enforcer.getClass().toString();
    }

    @Override
    public IEnvelopeFilterPredicate getFilterPredicate() {
        return new IEnvelopeFilterPredicate() {
            @Override
            public boolean filter(Envelope envelope) { return filterPredicate.apply(envelope); }
        };
    }


    @Override
    public Map<String, String> getRegistrationInfo() {

        return this.registrationInfo;
    }


    @Override
    public Object handle(Envelope envelope) throws Exception {

        adjudicator.adjudicate(envelope, enforcer);

        return true;
    }


    @Override
    public Object handleFailed(Envelope envelope, Exception e) throws Exception {

        LOG.error("Failed to handle Envelope!", e);

        return false;
    }


    public static class AdjudicateAllPredicate implements Predicate<Envelope> {

        @Override
        public boolean apply(@Nullable Envelope envelope) {

            return true;
        }
    }
}
