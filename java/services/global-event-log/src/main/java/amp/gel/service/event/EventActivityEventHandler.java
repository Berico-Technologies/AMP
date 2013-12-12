package amp.gel.service.event;

import java.util.Map;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import amp.rabbit.dispatch.DeliveryOutcomes;
import amp.gel.domain.Table;
import amp.gel.domain.TimeScale;
import amp.gel.service.EventActivityService;
import cmf.bus.Envelope;
import cmf.eventing.IEventBus;
import cmf.eventing.IEventHandler;

/**
 * FIXME: use RPC instead of publish/subscribe
 * 
 */
public class EventActivityEventHandler implements
		IEventHandler<EventActivityRequestEvent>, InitializingBean {
	private static final Logger logger = LoggerFactory
			.getLogger(EventActivityEventHandler.class);

	private IEventBus eventBus;

	private EventActivityService eventActivityService;

	public void setEventBus(IEventBus eventBus) {
		this.eventBus = eventBus;
	}

	public void setEventActivityService(
			EventActivityService eventActivityService) {
		this.eventActivityService = eventActivityService;
	}

	public Class<EventActivityRequestEvent> getEventType() {
		return EventActivityRequestEvent.class;
	}

	public Object handle(EventActivityRequestEvent requestEvent,
			Map<String, String> headers) {
		DateTime start = new DateTime(requestEvent.getStart().getTime());
		DateTime stop = new DateTime(requestEvent.getStop().getTime());
		TimeScale timeScale = requestEvent.getTimeScale();

		try {
			Table eventsByTime = eventActivityService.getEventsByTime(start,
					stop, timeScale);
			Table eventsByType = eventActivityService.getEventsByType(start,
					stop);
			Table eventsByUser = eventActivityService.getEventsByUser(start,
					stop);
			EventActivityResponseEvent responseEvent = new EventActivityResponseEvent(
					eventsByTime, eventsByType, eventsByUser);
			eventBus.publish(responseEvent);
		} catch (Exception e) {
			logger.error("Unable to get event activity", e);
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
