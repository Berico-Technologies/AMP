package amp.topology.snapshot;

import amp.topology.global.TopicRegistry;
import amp.topology.snapshot.impl.SnapshotSerializer;
import amp.topology.snapshot.impl.serializers.JsonSnapshotSerializer;
import amp.topology.snapshot.impl.serializers.XmlSnapshotSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Low and behold!  Simpler configuration!
 *
 * This class basically wires up the SnapshotResource for you by importing the SnapshotManager.
 *
 * @author Richard Clayton (Berico Technologies)
 */
@Configuration
public class SpringSnapshotConfig {

    /**
     * This should be defined somewhere in a Spring Context file.
     */
    @Autowired SnapshotManager snapshotManager;

    /**
     * This should be defined somewhere in a Spring Context file.
     */
    @Autowired TopicRegistry topicRegistry;

    @Bean(name = "snapshotResource")
    public SnapshotResource getSnapshotResource(){

        return new SnapshotResource(snapshotManager);
    }

    @Bean(name = "xmlSnapshotSerializer")
    @Primary
    public SnapshotSerializer getXmlSnapshotSerializer() {

        return new XmlSnapshotSerializer();
    }

    @Bean(name = "jsonSnapshotSerializer")
    public SnapshotSerializer getJsonSnapshotSerializer() {

        return new JsonSnapshotSerializer();
    }

    @Bean
    public SnapshotHealthCheck getSnapshotHealthCheck() {

        return new SnapshotHealthCheck(snapshotManager, topicRegistry);
    }
}
