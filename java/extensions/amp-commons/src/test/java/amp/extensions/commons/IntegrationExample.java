package amp.extensions.commons;

import static org.junit.Assert.*;

import amp.extensions.commons.builder.BuilderException;
import org.junit.Test;

import cmf.eventing.IEventBus;

import amp.extensions.commons.builder.BusBuilder;

public class IntegrationExample {

	@Test
	public void instantiate_event_bus_via_fluent_api() throws Exception, BuilderException {

        IEventBus eventBus =
            new BusBuilder()
                .configure()
                    .connectWith()
                        .basicAuth("guest", "guest")
                    .routeWith()
                        .globalTopology()
                            .usingHttp("localhost", 15677, "app01", "password")
                        .andThen()
                            .cacheRoutingWith().commandedCache(600)
                .and().eventBus().getInstance();

        eventBus.publish(new String("Hello"));

		IEventBus eventBus2 = BusBuilder.localProfile().eventBus();
	}
}