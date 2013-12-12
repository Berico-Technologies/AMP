package amp.gel.service.event;

import java.util.Map;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import amp.rabbit.DeliveryOutcomes;
import amp.gel.domain.Table;
import amp.gel.domain.TimeScale;
import amp.gel.service.TypeActivityService;
import cmf.bus.Envelope;
import cmf.eventing.IEventBus;
import cmf.eventing.IEventHandler;

/**
 * FIXME: use RPC instead of publish/subscribe
 * 
 */
public class TypeActivityEventHandler implements
		IEventHandler<TypeActivityRequestEvent>, InitializingBean {
	private static final Logger logger = LoggerFactory
			.getLogger(TypeActivityEventHandler.class);

	private IEventBus eventBus;

	private TypeActivityService typeActivityService;

	public void setEventBus(IEventBus eventBus) {
		this.eventBus = eventBus;
	}

	public void setTypeActivityService(TypeActivityService typeActivityService) {
		this.typeActivityService = typeActivityService;
	}

	public Class<TypeActivityRequestEvent> getEventType() {
		return TypeActivityRequestEvent.class;
	}

	public Object handle(TypeActivityRequestEvent requestEvent,
			Map<String, String> headers) {
		String type = requestEvent.getType();
		DateTime start = new DateTime(requestEvent.getStart().getTime());
		DateTime stop = new DateTime(requestEvent.getStop().getTime());
		TimeScale timeScale = requestEvent.getTimeScale();

		try {
			Table eventsByTime = typeActivityService.getEventsByTime(start,
					stop, timeScale, type);
			Table eventsByUser = typeActivityService.getEventsByUser(start,
					stop, type);
			TypeActivityResponseEvent responseEvent = new TypeActivityResponseEvent(
					eventsByTime, eventsByUser);
			eventBus.publish(responseEvent);
		} catch (Exception e) {
			logger.error("Unable to get type activity", e);
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
