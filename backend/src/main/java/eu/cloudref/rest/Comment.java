package eu.cloudref.rest;

import eu.cloudref.dal.CommentService;
import eu.cloudref.dal.ReferencesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.Calendar;
import java.util.List;

@Api
@Path("references")
public class Comment {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"USER", "MAINTAINER"})
    @Path("{bibtexkey}/pdf/comments")
    public List<eu.cloudref.db.Comment> getPdfComments(@PathParam("bibtexkey") String bibtexkey, @Context SecurityContext sc) {

        if (ReferencesService.getReference(bibtexkey, false) == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .entity("No reference found with BibTeX-key: " + bibtexkey).build());
        }

        return CommentService.getComments(bibtexkey, sc.getUserPrincipal().getName());
    }

    @POST
    @RolesAllowed({"USER", "MAINTAINER"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{bibtexkey}/pdf/comments")
    public Response saveComment(@ApiParam(required = true) eu.cloudref.db.Comment newComment, @Context UriInfo uriInfo,
                                @PathParam("bibtexkey") String bibtexkey, @Context SecurityContext sc) {

        if (bibtexkey == null || bibtexkey.isEmpty()) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid request parameter" + bibtexkey).build());
        }

        if (newComment == null || !bibtexkey.equals(newComment.getBibtexkey())) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid request body" + bibtexkey).build());
        }

        String username = sc.getUserPrincipal().getName();

        if (username == null || !username.equals(newComment.getAuthor())) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid request - user is not author of comment").build());
        }

        BibTeXEntry reference = ReferencesService.getReference(bibtexkey, true);

        if (reference == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .entity("No reference found with BibTeX-key: " + bibtexkey).build());
        }

        if (reference.getField(new Key("Pdf")).equals("false")) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .entity("No PDF file found for reference with BibTeX-key: " + bibtexkey).build());
        }

        // set current time
        newComment.setDate(Calendar.getInstance());

        // add comment
        eu.cloudref.db.Comment comment = CommentService.addComment(newComment);

        if (comment == null) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }

        UriBuilder path = uriInfo.getAbsolutePathBuilder();
        path.path(Integer.toString(comment.getId()));
        return Response.created(path.build()).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"USER", "MAINTAINER"})
    @Path("{bibtexkey}/pdf/comments/{id}")
    public Response updateComment(@ApiParam(required = true) eu.cloudref.db.Comment updatedComment, @Context SecurityContext sc,
                                  @PathParam("bibtexkey") String bibtexkey, @PathParam("id") int id) {

        // check if request body is valid
        if (updatedComment == null || !updatedComment.getBibtexkey().equals(bibtexkey) || updatedComment.getId() != id) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid request body").build());
        }

        // get user and username
        String username = sc.getUserPrincipal().getName();

        // check if user has rights to delete comment
        if (username == null || updatedComment.getAuthor() == null || !username.equals(updatedComment.getAuthor())) {
            throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN)
                    .entity("The user does not have the necessary rights to edit this comment").build());
        }

        // get comment from backend
        eu.cloudref.db.Comment comment = CommentService.getComment(bibtexkey, id);

        // check if comment for update exists
        if (comment == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .entity("No comment found with bibtexkey '" + bibtexkey + "' and id: " + id).build());
        }

        // check if authors of comments are the same
        if (!comment.getAuthor().equals(updatedComment.getAuthor())) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid request body").build());
        }

        // set current time as alteration date
        updatedComment.setAlterationDate(Calendar.getInstance());

        eu.cloudref.db.Comment c = CommentService.updateComment(updatedComment);

        if (c == null) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }

        return Response.ok().entity(c).build();
    }

    @DELETE
    @RolesAllowed({"USER", "MAINTAINER"})
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{bibtexkey}/pdf/comments/{id}")
    public void deleteComment(@Context SecurityContext sc, @PathParam("bibtexkey") String bibtexkey,
                              @PathParam("id") int commentId) {

        if (bibtexkey == null || bibtexkey.isEmpty() || commentId <= 0) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid request parameter").build());
        }

        String username = sc.getUserPrincipal().getName();

        // get comment from backend
        eu.cloudref.db.Comment comment = CommentService.getComment(bibtexkey, commentId);

        // check if username and author of comment are the same
        if (comment == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .entity("No comment found of bibtexkey '" + bibtexkey + "' with id: " + commentId).build());
        }
        if (username == null || comment.getAuthor() == null) {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
        // allow only to delete own comments
        if (!username.equals(comment.getAuthor())) {
            throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN)
                    .entity("User is not allowed to delete comment of '" + bibtexkey + "' with id: " + commentId).build());
        }

        // delete comment
        if (!CommentService.deleteComment(bibtexkey, commentId)) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
    }
}
