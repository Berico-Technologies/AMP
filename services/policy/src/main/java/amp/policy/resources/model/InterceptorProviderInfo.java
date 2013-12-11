package amp.policy.resources.model;

import amp.policy.core.EnvelopeInterceptor;
import amp.policy.core.InterceptorProvider;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class InterceptorProviderInfo {

    private InterceptorProviderInfo(){}

    String provider;

    List<EnvelopeInterceptorInfo> interceptors;

    public String getProvider() {
        return provider;
    }

    public List<EnvelopeInterceptorInfo> getInterceptors() {
        return interceptors;
    }

    public static InterceptorProviderInfo get(InterceptorProvider provider){

        InterceptorProviderInfo info = new InterceptorProviderInfo();

        info.provider = provider.getClass().getCanonicalName();

        info.interceptors = Lists.newArrayList();

        for (EnvelopeInterceptor interceptor : provider.get()){

            info.interceptors.add(EnvelopeInterceptorInfo.get(interceptor));
        }

        return info;
    }
}