package amp.policy.resources.model;

import amp.policy.core.EnvelopeInterceptor;

import java.util.Map;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class EnvelopeInterceptorInfo {

    private EnvelopeInterceptorInfo(){}

    String id;

    String description;

    String adjudicator;

    String enforcer;

    Map<String, String> registrationInfo;

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getAdjudicator() {
        return adjudicator;
    }

    public String getEnforcer() {
        return enforcer;
    }

    public Map<String, String> getRegistrationInfo() {
        return registrationInfo;
    }

    public static EnvelopeInterceptorInfo get(EnvelopeInterceptor interceptor){

        EnvelopeInterceptorInfo info = new EnvelopeInterceptorInfo();

        info.id = interceptor.getId();

        info.description = interceptor.getDescription();

        info.adjudicator = interceptor.getAdjudicatorType();

        info.enforcer = interceptor.getEnforcerType();

        info.registrationInfo = interceptor.getRegistrationInfo();

        return info;
    }
}
