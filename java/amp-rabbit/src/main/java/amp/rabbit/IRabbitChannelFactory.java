package amp.rabbit;


import cmf.bus.IDisposable;
import com.rabbitmq.client.Channel;

import amp.rabbit.topology.Exchange;


public interface IRabbitChannelFactory extends IDisposable {

	Channel getChannelFor(Exchange exchange) throws Exception;
}
