package amp.policy.core.providers;

import amp.policy.core.EnvelopeAdjudicator;
import amp.policy.core.EnvelopeInterceptor;
import amp.policy.core.InterceptionCriteria;
import amp.policy.core.PolicyEnforcer;
import com.google.common.base.Splitter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Policy Interceptor - this can be used to adorn EnvelopeInterceptors or
 * EnvelopeAdjudicators so they can be automatically registered by the
 * SpringInterceptorProvider.
 *
 * @author Richard Clayton (Berico Technologies)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PolicyInterceptor {

    public static final String DEFAULT_VALUE = "__default__";

    String id() default DEFAULT_VALUE;

    String description() default DEFAULT_VALUE;

    String enforcer() default DEFAULT_VALUE;

    String value();

    String sender() default DEFAULT_VALUE;

    String regInfo() default DEFAULT_VALUE;

    /**
     * Encapsulates all the logic needed to pull out context from the annotation.
     */
    public static class Helper {

        public static final String RECORD_DELIMITER = "&";

        public static final String KEY_VALUE_DELIMITER = "=";

        public static Map<String, String> getRegistrationInfo(Object annotatedTarget){

            PolicyInterceptor info = annotatedTarget.getClass().getAnnotation(PolicyInterceptor.class);

            InterceptionCriteria criteria = new InterceptionCriteria();

            criteria.setTopic(info.value());

            if (!info.sender().equals(DEFAULT_VALUE)){

                criteria.setSenderIdentity(info.sender());
            }

            if (!info.regInfo().equals(DEFAULT_VALUE)){

                Map<String, String> registrationInfo =
                        Splitter.on(RECORD_DELIMITER)
                                .omitEmptyStrings()
                                .trimResults()
                                .withKeyValueSeparator(KEY_VALUE_DELIMITER)
                                .split(info.regInfo());

                criteria.putAll(registrationInfo);
            }

            return criteria;
        }

        public static EnvelopeInterceptor createInterceptor(
                EnvelopeAdjudicator annotatedAdjudicator, PolicyEnforcer enforcer){

            PolicyInterceptor info = annotatedAdjudicator.getClass().getAnnotation(PolicyInterceptor.class);

            Map<String, String> registrationInfo = getRegistrationInfo(annotatedAdjudicator);

            String id = (info.id().equals(DEFAULT_VALUE))? UUID.randomUUID().toString() : info.id();

            String description = (info.description().equals(DEFAULT_VALUE))?
                    annotatedAdjudicator.getClass().getCanonicalName() : info.description();

            return new EnvelopeInterceptor(
                    id, description, annotatedAdjudicator, enforcer, registrationInfo);
        }

    }
}
