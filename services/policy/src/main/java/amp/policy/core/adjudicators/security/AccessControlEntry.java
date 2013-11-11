package amp.policy.core.adjudicators.security;

/**
 *
 *
 */
public class AccessControlEntry {

    public enum Permissions {
        APPROVED,
        DENIED
    }

    private Permissions permission = Permissions.APPROVED;

    private String principal;

    public AccessControlEntry(String principal) {
        this.principal = principal;
    }

    public AccessControlEntry(Permissions permission, String principal) {
        this.permission = permission;
        this.principal = principal;
    }

    public Permissions getPermission() {
        return permission;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPermission(Permissions permission) {
        this.permission = permission;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }
}
