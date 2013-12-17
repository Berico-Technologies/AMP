package amp.topology.global.anubis;

/**
 * An evaluation of whether an actor should be allowed to do something.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public interface AccessControl {

    boolean isAllowed(Actor actor);
}
