package amp.topology.client;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import amp.rabbit.topology.RouteInfo;
import amp.rabbit.topology.RoutingInfo;
import amp.utility.serialization.ISerializer;

/**
 * This is a fix until the Iterable is removed from RoutingInfo.
 * 
 * @author Richard Clayton (Berico Technologies)
 */
public class JsonRoutingInfoSerializer implements ISerializer {

	private static Logger logger = 
		LoggerFactory.getLogger(JsonRoutingInfoSerializer.class);
	
	Gson gson = new Gson();
	
	public JsonRoutingInfoSerializer(){}
	
	@Override
	public <TYPE> TYPE byteDeserialize(byte[] payload, Class<TYPE> clazz) {
		
		return stringDeserialize(new String(payload), clazz);
	}

	@Override
	public byte[] byteSerialize(Object objectToSerializer) {
		
		return stringSerialize(objectToSerializer).getBytes();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <TYPE> TYPE stringDeserialize(String payload, Class<TYPE> clazz) {
		
		logger.debug("deserializing object.");
		
		if (clazz == RoutingInfo.class){
			
			logger.debug("Is a routing info object.");
			
			ArrayList<RouteInfo> routeInfos = new ArrayList<RouteInfo>();
			
			JsonParser parser = new JsonParser();
			JsonObject o = (JsonObject)parser.parse(payload);
			JsonArray routes = o.getAsJsonArray("routes");
			
			for(JsonElement route : routes){
				
				routeInfos.add(gson.fromJson(route, RouteInfo.class));
			}
			
			return (TYPE) new RoutingInfo(routeInfos);
		}
		else {
			
			return gson.fromJson(payload, clazz);
		}
	}

	@Override
	public String stringSerialize(Object objectToSerializer) {
		
		return gson.toJson(objectToSerializer);
	}	
}