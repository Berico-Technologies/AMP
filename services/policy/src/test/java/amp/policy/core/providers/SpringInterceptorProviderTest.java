package amp.policy.core.providers;

import amp.policy.core.EnvelopeInterceptor;
import amp.policy.core.InterceptorProvider;
import amp.policy.core.impl.DefaultEnforcer;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Richard Clayton (Berico Technologies)
 */
public class SpringInterceptorProviderTest {

    @Test
   public void effectively_retrieves_interceptors_from_the_application_context(){

        GenericApplicationContext context = new GenericXmlApplicationContext("springProviderTestContext.xml");

        InterceptorProvider provider = context.getBean(SpringInterceptorProvider.class);

        Collection<EnvelopeInterceptor> interceptors = provider.get();

        assertEquals(3, interceptors.size());

        ArrayList<String> adjClasses = Lists.newArrayList();

        for(EnvelopeInterceptor interceptor : interceptors){

            if (interceptor.getAdjudicatorType().equals(MockAdjudicator2.class.toString())){

                assertEquals(MockEnforcer.class.toString(), interceptor.getEnforcerType());
            }
            else {

                assertEquals(DefaultEnforcer.class.toString(), interceptor.getEnforcerType());
            }

            adjClasses.add(interceptor.getAdjudicatorType());
        }

        assertTrue(adjClasses.contains(MockAdjudicator1.class.toString()));
        assertTrue(adjClasses.contains(MockAdjudicator2.class.toString()));
        assertTrue(adjClasses.contains(MockAdjudicator3.class.toString()));
   }

}
