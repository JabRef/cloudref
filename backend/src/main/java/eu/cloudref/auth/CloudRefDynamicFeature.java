package eu.cloudref.auth;

import eu.cloudref.db.User;
import org.glassfish.jersey.internal.util.Base64;
import org.glassfish.jersey.server.model.AnnotatedMethod;

import javax.annotation.Priority;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.*;

public class CloudRefDynamicFeature implements DynamicFeature {

    @Override
    public void configure(final ResourceInfo resourceInfo, final FeatureContext configuration) {
        final AnnotatedMethod am = new AnnotatedMethod(resourceInfo.getResourceMethod());

        // DenyAll on the method take precedence over RolesAllowed and PermitAll
        if (am.isAnnotationPresent(DenyAll.class)) {
            configuration.register(new CloudRefRequestFilter());
            return;
        }

        // RolesAllowed on the method takes precedence over PermitAll
        RolesAllowed ra = am.getAnnotation(RolesAllowed.class);
        if (ra != null) {
            configuration.register(new CloudRefRequestFilter(ra.value()));
            return;
        }

        // PermitAll takes precedence over RolesAllowed on the class
        if (am.isAnnotationPresent(PermitAll.class)) {
            // Do nothing.
            return;
        }

        // DenyAll can't be attached to classes

        // RolesAllowed on the class takes precedence over PermitAll
        ra = resourceInfo.getResourceClass().getAnnotation(RolesAllowed.class);
        if (ra != null) {
            configuration.register(new CloudRefRequestFilter(ra.value()));
        }
    }

    @Priority(Priorities.AUTHORIZATION)
    private static class CloudRefRequestFilter implements ContainerRequestFilter {

        private final boolean denyAll;
        private final String[] rolesAllowed;
        @Context
        private ResourceInfo resourceInfo;

        private static final String AUTHORIZATION_PROPERTY = "Authorization";
        private static final String AUTHENTICATION_SCHEME = "Basic";

        CloudRefRequestFilter() {
            this.denyAll = true;
            this.rolesAllowed = null;
        }

        CloudRefRequestFilter(final String[] rolesAllowed) {
            this.denyAll = false;
            this.rolesAllowed = (rolesAllowed != null) ? rolesAllowed : new String[]{};
        }

        @Override
        public void filter(final ContainerRequestContext requestContext) throws IOException {
            if (!denyAll) {
                //Get request headers
                final MultivaluedMap<String, String> headers = requestContext.getHeaders();

                //Fetch authorization header
                final List<String> authorization = headers.get(AUTHORIZATION_PROPERTY);

                //If no authorization information present; block access
                if (authorization == null || authorization.isEmpty()) {
                    requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("User is not authorized.").build());
                    return;
                }

                //Get encoded username and password
                final String encodedUserPassword = authorization.get(0).replaceFirst(AUTHENTICATION_SCHEME + " ", "");

                //Decode username and password
                String usernameAndPassword = new String(Base64.decode(encodedUserPassword.getBytes()));

                //Split username and password tokens
                final StringTokenizer tokenizer = new StringTokenizer(usernameAndPassword, ":");
                final String username = tokenizer.nextToken();
                final String password = tokenizer.nextToken();

                //Verify user access
                if (this.rolesAllowed != null) {
                    Set<String> rolesSet = new HashSet<>(Arrays.asList(rolesAllowed));
                    // get user from database
                    User user = AuthService.getUser(username);
                    // is user information valid
                    if (isUserInformationValid(user, username, password)) {
                        // is user allowed to access
                        if (isUserAllowed(user, rolesSet)) {
                            // set SecurityContext
                            requestContext.setSecurityContext(new CloudRefSecurityContext(user));
                        } else {
                            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).entity("User does not have the necessary rights to access.").build());
                        }
                    } else {
                        // user information is not valid
                        requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("User information is not valid.").build());
                    }
                }
            } else {
                requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).entity("Access blocked for all users!").build());
            }
        }

        private boolean isUserInformationValid(User user, final String username, final String password) {
            boolean isValid = false;

            if (user != null && username.equals(user.getName())) {
                byte[] salt = user.getSalt();
                // check if hashed passwords match
                if (AuthService.encodePassword(password, salt).equals(user.getPassword())) {
                    isValid = true;
                }
            }
            return isValid;
        }

        private boolean isUserAllowed(User user, final Set<String> rolesSet) {
            boolean isAllowed = false;
            String userRole = user.getUserRole().getName();

            // verify user role
            if (rolesSet.contains(userRole)) {
                isAllowed = true;
            }
            return isAllowed;
        }
    }
}

