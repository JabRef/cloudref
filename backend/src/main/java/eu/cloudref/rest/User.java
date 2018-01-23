package eu.cloudref.rest;

import java.net.URI;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import eu.cloudref.auth.AuthService;
import eu.cloudref.models.Role;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

@Api
@Path("")
public class User {

    /**
     * Redirect to cloudref UI
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getRedirect() {
        return Response.temporaryRedirect(URI.create("cloudref/")).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("login")
    @RolesAllowed({"USER", "MAINTAINER"})
    public Role loginUser(@Context SecurityContext sc) {
        // if user has necessary rights return his role
        Role r = ((eu.cloudref.db.User) sc.getUserPrincipal()).getUserRole();
        return r;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("users/{username}")
    @PermitAll
    public Response saveUser(@PathParam("username") String username, @ApiParam(required = true) eu.cloudref.db.User newUser,
                             @Context UriInfo uriInfo) {

        if (newUser == null || !newUser.getName().equals(username)) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid request body").build());
        }

        // create new user
        eu.cloudref.db.User createdUser = AuthService.addUser(newUser);
        // check if user was created
        if (createdUser == null) {
            throw new WebApplicationException(Response.status(Response.Status.CONFLICT)
                    .entity("Username already exists").build());
        }

        // do not return user object
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
