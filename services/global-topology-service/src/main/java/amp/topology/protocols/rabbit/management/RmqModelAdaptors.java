package amp.topology.protocols.rabbit.management;

import com.google.common.collect.Maps;
import rabbitmq.mgmt.model.Exchange;
import rabbitmq.mgmt.model.Queue;

import java.util.Map;

/**
 * TODO: Perhaps move RabbitMQ-Management-Java into GTS project to avoid the conversion.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class RmqModelAdaptors {

    public static Exchange to(amp.rabbit.topology.Exchange exchange, String virtualHost){

        Exchange ex = new Exchange();

        ex.setName(exchange.getName());
        ex.setType(exchange.getExchangeType());
        ex.setAutoDelete(exchange.isAutoDelete());
        ex.setDurable(exchange.isDurable());
        ex.setInternal(false);
        ex.setVhost(virtualHost);
        ex.setArguments(convertToMapSS(exchange.getArguments()));

        return ex;
    }

    public static amp.rabbit.topology.Exchange from(Exchange exchange){

        return amp.rabbit.topology.Exchange.builder()
                .name(exchange.getName())
                .type(exchange.getType())
                .isAutoDelete(exchange.isAutoDelete())
                .isDurable(exchange.isDurable())
                .arguments(convertToMapSO(exchange.getArguments()))
                .declare(false)
                .build();
    }

    public static Queue to(amp.rabbit.topology.Queue queue, String virtualHost){

        Queue q = new Queue();

        q.setName(queue.getName());
        q.setAutoDelete(queue.isAutoDelete());
        q.setDurable(queue.isDurable());
        q.setArguments(convertToMapSS(queue.getArguments()));
        q.setVhost(virtualHost);

        return q;
    }

    public static amp.rabbit.topology.Queue from(Queue queue){

        return amp.rabbit.topology.Queue.builder()
                .name(queue.getName())
                .isAutoDelete(queue.isAutoDelete())
                .isDurable(queue.isDurable())
                .arguments(convertToMapSO(queue.getArguments()))
                .declare(false)
                .build();
    }

    public static Map<String, String> convertToMapSS(Map<String, Object> map){

        Map<String, String> newMap = Maps.newHashMap();

        for (Map.Entry<String, Object> e : map.entrySet())
            newMap.put(e.getKey(), e.getValue().toString());

        return newMap;
    }

    public static Map<String, Object> convertToMapSO(Map<String, String> map){

        Map<String, Object> newMap = Maps.newHashMap();

        for (Map.Entry<String, String> e : map.entrySet())
            newMap.put(e.getKey(), e.getValue());

        return newMap;
    }
}
