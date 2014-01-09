package amp.topology.anubis;

/**
 * Is this object Access Controlled?
 *
 * @author Richard Clayton (Berico Technologies)
 */
public interface AccessControlled {

    /**
     * Get the AccessControlList for this object.
     * @return AccessControlList
     */
    AccessControlList getACL();
}
