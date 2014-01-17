package amp.topology.factory.impl;

import amp.topology.factory.ConnectorFactory;
import amp.topology.factory.GroupFactory;
import amp.topology.factory.TopicFactory;
import amp.topology.global.TopicRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

/**
 * Configures the Factories for constructing/modifying topology items.
 *
 * @author Richard Clayton (Berico Technologies)
 */
@Configuration
public class FactoryConfig implements ApplicationContextAware {

    @Autowired
    TopicRegistry topicRegistry;

    GroupFactory groupFactory;

    ConnectorFactory connectorFactory;

    ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        this.applicationContext = applicationContext;
    }

    @Bean
    public TopicFactory getTopicFactory(){

        return new BasicTopicFactory(topicRegistry, getGroupFactory(), getConnectorFactory());
    }

    @Bean
    public GroupFactory getGroupFactory(){

        if (this.groupFactory == null){

            Collection<DelegatingGroupFactory.GroupFactoryDelegate> delegates =
                this.applicationContext.getBeansOfType(
                    DelegatingGroupFactory.GroupFactoryDelegate.class).values();

            this.groupFactory = new DelegatingGroupFactory(topicRegistry, delegates);
        }
        return this.groupFactory;
    }

    @Bean
    public ConnectorFactory getConnectorFactory(){

        if (this.connectorFactory == null){

            Collection<DelegatingConnectorFactory.ConnectorFactoryDelegate> delegates =
                this.applicationContext.getBeansOfType(
                    DelegatingConnectorFactory.ConnectorFactoryDelegate.class).values();

            this.connectorFactory = new DelegatingConnectorFactory(topicRegistry, delegates);
        }

        return this.connectorFactory;
    }

}
