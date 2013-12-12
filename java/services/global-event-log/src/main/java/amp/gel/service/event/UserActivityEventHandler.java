package amp.gel.service.event;

import java.util.Map;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import amp.rabbit.DeliveryOutcomes;
import amp.gel.domain.Table;
import amp.gel.domain.TimeScale;
import amp.gel.service.UserActivityService;
import cmf.bus.Envelope;
import cmf.eventing.IEventBus;
import cmf.eventing.IEventHandler;

/**
 * FIXME: use RPC instead of publish/subscribe
 * 
 */
public class UserActivityEventHandler implements
		IEventHandler<UserActivityRequestEvent>, InitializingBean {
	private static final Logger logger = LoggerFactory
			.getLogger(UserActivityEventHandler.class);

	private IEventBus eventBus;

	private UserActivityService userActivityService;

	public void setEventBus(IEventBus eventBus) {
		this.eventBus = eventBus;
	}

	public void setUserActivityService(UserActivityService userActivityService) {
		this.userActivityService = userActivityService;
	}

	public Class<UserActivityRequestEvent> getEventType() {
		return UserActivityRequestEvent.class;
	}

	public Object handle(UserActivityRequestEvent requestEvent,
			Map<String, String> headers) {
		String user = requestEvent.getUser();
		DateTime start = new DateTime(requestEvent.getStart().getTime());
		DateTime stop = new DateTime(requestEvent.getStop().getTime());
		TimeScale timeScale = requestEvent.getTimeScale();

		try {
			Table eventsByTime = userActivityService.getEventsByTime(start,
					stop, timeScale, user);
			Table eventsByType = userActivityService.getEventsByType(start,
					stop, user);
			UserActivityResponseEvent responseEvent = new UserActivityResponseEvent(
					eventsByTime, eventsByType);
			eventBus.publish(responseEvent);
		} catch (Exception e) {
			logger.error("Unable to get user activity", e);
		}

		return DeliveryOutcomes.Acknowledge;
	}

	public Object handleFailed(Envelope envelope, Exception ex) {
		return DeliveryOutcomes.Exception;
	}

	public void afterPropertiesSet() throws Exception {
		eventBus.subscribe(this);
	}
}
