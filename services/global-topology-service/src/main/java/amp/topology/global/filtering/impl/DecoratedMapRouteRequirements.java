package amp.topology.global.filtering.impl;

import amp.topology.Constants;
import amp.topology.anubis.Actor;
import amp.topology.global.filtering.RouteRequirements;

import java.util.HashMap;
import java.util.Map;

/**
 * Takes some of the boilerplate out of converting a Map into a RouteRequirements object.
 *
 * This implementation assumes that Actor will be supplied externally, probably from the
 * request context of a RESTful call.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public abstract class DecoratedMapRouteRequirements extends HashMap<String, String> implements RouteRequirements {

    private Actor actor;

    protected DecoratedMapRouteRequirements(Map<String, String> routeInfo) throws IllegalStateException {

        if (routeInfo != null && routeInfo.size() > 0){

            this.putAll(routeInfo);
        }

        validate();
    }

    protected void validate() throws IllegalStateException {

        // Check to ensure all the values are valid.
    }

    @Override
    public String getTopic() {

        return this.get(Constants.MESSAGE_TOPIC);
    }

    @Override
    public String getMessagePattern() {

        return this.get(Constants.MESSAGE_PATTERN);
    }

    @Override
    public void setActor(Actor actor) {
        this.actor = actor;
    }

    @Override
    public Actor getActor() {

        return this.actor;
    }

    @Override
    public RouteDirections getRouteDirection() {

        String messageDirection = this.get(Constants.MESSAGE_DIRECTION);

        return RouteDirections.valueOf(messageDirection);

    }
}
