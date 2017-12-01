package eu.cloudref.auth;

import eu.cloudref.db.User;

import javax.ws.rs.core.SecurityContext;

public class CloudRefSecurityContext implements SecurityContext {

    User user;

    public CloudRefSecurityContext(User user) {
        this.user = user;
    }

    @Override
    public User getUserPrincipal() {
        return this.user;
    }

    @Override
    public boolean isUserInRole(String role) {
        return this.user.getUserRole().getName().equals(role);
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public String getAuthenticationScheme() {
        return SecurityContext.BASIC_AUTH;
    }
}
