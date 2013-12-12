package amp.topology.core.repo.listeners;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import amp.topology.core.ExtendedExchange;
import amp.topology.core.ExtendedRouteInfo;
import amp.topology.core.ITopologyRepositoryEventListener;

public class NoRouteInfoForTopicListener implements ITopologyRepositoryEventListener {
	
	private static final Logger logger = LoggerFactory.getLogger(NoRouteInfoForTopicListener.class);
	
	ConcurrentHashMap<String, TopicMiss> topicMissMap = 
			new ConcurrentHashMap<String, TopicMiss>();

	public NoRouteInfoForTopicListener(){}
	
	@Override
	public void routingInfoRetrieved(
			String topic, String client,
			Collection<ExtendedRouteInfo> routingInfo) {
		
		/**
		 * No route info for topic!
		 */
		if (routingInfo.size() == 0 && topic != null){
			
			logger.debug("Topic miss for {}", topic);
			
			TopicMiss topicMiss = topicMissMap.get(topic);
			
			if (topicMiss == null){
				
				TopicMiss newTopicMiss = new TopicMiss(topic);
				
				topicMiss = topicMissMap.putIfAbsent(topic, newTopicMiss);
				
				if(topicMiss == null){
					
					topicMiss = newTopicMiss;
				}
			}
			
			topicMiss.logMiss(client);
		}
		else if (topic == null){
			
			logger.warn("Client '{}' tried to retrieve a route for a NULL topic.", client);
		}
	}
	
	/**
	 * Get all topic misses.
	 * @return UNMODIFIABLE COLLECTION of Topic Misses.
	 */
	public Collection<TopicMiss> getTopicMisses(){
		
		return Collections.unmodifiableCollection(this.topicMissMap.values());
	}
	
	/* ####### Irrelevant Methods ########################################################## */
	
	@Override
	public void routeCreated(ExtendedRouteInfo routeInfo) {}

	@Override
	public void routeRemoved(ExtendedRouteInfo routeInfo) {}

	@Override
	public void routeUpdated(ExtendedRouteInfo oldRouteInfo, ExtendedRouteInfo newRouteInfo) {}

	@Override
	public void exchangeCreated(ExtendedExchange exchange) {}

	@Override
	public void exchangeRemoved(ExtendedExchange exchange) {}

	@Override
	public void exchangeUpdated(ExtendedExchange oldExchange, ExtendedExchange newExchange) {}
}
