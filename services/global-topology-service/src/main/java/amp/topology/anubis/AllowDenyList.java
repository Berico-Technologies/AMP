package amp.topology.anubis;

import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * An Access Control Entry.
 *
 * This class represents a rule as to whether an Actor or Group is allowed to perform
 * the specified operation.
 *
 * @author Richard Clayton (Berico Technologies)
 */
public class AllowDenyList implements AccessControl {

    public enum DefaultPrivilege {
        /**
         * No default privilege.  Basically, if you are not allowed, you're denied.
         */
        None,
        /**
         * Allow anyone, but deny if a user belongs to either the denied actors or groups list.
         */
        AllowAll_ImplicitDeny,
        /**
         * Allow anyone, but deny only if a user is on the denied actors list.
         */
        AllowAll_ExplicitDeny,
        /**
         * Deny everyone unless they belong to the allowed actors or groups list.
         */
        DenyAll_ImplicitAllow,
        /**
         * Deny everyone unless they belong to the allowed actors list.
         */
        DenyAll_ExplicitAllow,
    }

    private DefaultPrivilege defaultPrivilege;

    private CopyOnWriteArraySet<String> allowedActors = Sets.newCopyOnWriteArraySet();

    private CopyOnWriteArraySet<String> allowedGroups = Sets.newCopyOnWriteArraySet();

    private CopyOnWriteArraySet<String> deniedActors = Sets.newCopyOnWriteArraySet();

    private CopyOnWriteArraySet<String> deniedGroups = Sets.newCopyOnWriteArraySet();

    @Override
    public boolean isAllowed(Actor actor){

        switch (defaultPrivilege) {
            // TRUE if the actor is not on the denied actors list, and doesn't belong to a denied group.
            case AllowAll_ImplicitDeny:
                return !deniedActors.contains(actor.getId())
                    // Collections.disjoint:
                    // Returns true if the two specified collections have no elements in common.
                    && Collections.disjoint(deniedGroups, actor.getMembershipIds());
            // TRUE if the actor is not on the denied actors list
            case AllowAll_ExplicitDeny:
                return !deniedActors.contains(actor.getId());
            // TRUE if the actor is in the allowed actors list
            case DenyAll_ExplicitAllow:
                return allowedActors.contains(actor.getId());
            // TRUE if the actor is in the allowed actors list or in the allowed groups list.
            default:
                // DenyAll_ImplicitAllow and None are essentially the same thing.
                return allowedActors.contains(actor.getId())
                    // Collections.disjoint:
                    // Returns true if the two specified collections have no elements in common.
                    || !Collections.disjoint(allowedGroups, actor.getMembershipIds());
        }
    }

    /**
     * Initialize the AllowDenyList with the default state.  Nulls or empty collections are valid for the
     * allowed and denied actor and group lists.
     * @param defaultPrivilege Default privilege for the operation.
     * @param allowedActors Actors that are allowed to execute operation/access datum.
     * @param allowedGroups Groups that are allowed to execute operation/access datum.
     * @param deniedActors Actors that are not allowed to execute operation/access datum.
     * @param deniedGroups Groups that are not allowed to execute operation/access datum.
     */
    public AllowDenyList(
            DefaultPrivilege defaultPrivilege,
            Collection<String> allowedActors,
            Collection<String> allowedGroups,
            Collection<String> deniedActors,
            Collection<String> deniedGroups) {

        this.defaultPrivilege = defaultPrivilege;
        addAllIfNotEmpty(this.allowedActors, allowedActors);
        addAllIfNotEmpty(this.allowedGroups, allowedGroups);
        addAllIfNotEmpty(this.deniedActors, deniedActors);
        addAllIfNotEmpty(this.deniedGroups, deniedGroups);
    }

    public Collection<String> getAllowedActors() {
        return Collections.unmodifiableCollection(this.allowedActors);
    }

    public Collection<String> getAllowedGroups() {
        return Collections.unmodifiableCollection(this.allowedGroups);
    }

    public Collection<String> getDeniedActors() {
        return Collections.unmodifiableCollection(this.deniedActors);
    }

    public Collection<String> getDeniedGroups() {
        return Collections.unmodifiableCollection(this.deniedGroups);
    }

    public void allowActor(String actorId){

        synchronizeSets(this.allowedActors, this.deniedActors, actorId);
    }

    public void allowGroup(String groupId){

        synchronizeSets(this.allowedGroups, this.deniedGroups, groupId);
    }

    public void denyActor(String actorId){

        synchronizeSets(this.deniedActors, this.allowedActors, actorId);
    }

    public void denyGroup(String groupId){

        synchronizeSets(this.deniedGroups, this.allowedGroups, groupId);
    }

    public DefaultPrivilege getDefaultPrivilege() {
        return defaultPrivilege;
    }

    public void setDefaultPrivilege(DefaultPrivilege defaultPrivilege) {
        this.defaultPrivilege = defaultPrivilege;
    }

    static void addAllIfNotEmpty(CopyOnWriteArraySet<String> sourceList, Collection<String> newItems){

        if (newItems != null && newItems.size() > 0){

            sourceList.addAll(newItems);
        }
    }

    static void synchronizeSets(Set<String> setItemShouldBeIn, Set<String> setItemShouldNotBeIn, String item){

        if (setItemShouldNotBeIn.contains(item)){

            setItemShouldNotBeIn.remove(item);
        }

        setItemShouldBeIn.add(item);
    }
}
