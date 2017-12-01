package eu.cloudref.rest;

import eu.cloudref.dal.GitService;
import eu.cloudref.dal.ReferencesService;
import eu.cloudref.db.User;
import eu.cloudref.models.MergeInstruction;
import eu.cloudref.models.Rating;
import eu.cloudref.models.ResponseRatingSuggestion;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.jbibtex.BibTeXEntry;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.Map;

@Api
@Path("references")
public class Suggestion {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"MAINTAINER"})
    @Path("{bibtexkey}/suggestions/{id}")
    public BibTeXEntry getSuggestion(@PathParam("bibtexkey") String bibtexkey, @PathParam("id") int id) {

        if (id < 1 || bibtexkey == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid request body").build());
        }

        // check if reference exists
        if (ReferencesService.getReference(bibtexkey, false) == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .entity("No reference found with BibTeX-key: " + bibtexkey).build());
        }

        // check if suggestion exists
        if (id < 1 || !GitService.existsSuggestion(bibtexkey, id)) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .entity("No suggestion with id " + id + " found for BibTeX-key: " + bibtexkey).build());
        }

        // get suggestion
        BibTeXEntry suggestion = GitService.getSuggestion(bibtexkey, id);

        if (suggestion == null) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }

        return suggestion;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"USER", "MAINTAINER"})
    @Path("{bibtexkey}/suggestions")
    public List<Map<String, String>> getSuggestions(@PathParam("bibtexkey") String bibtexkey, @Context SecurityContext sc) {

        BibTeXEntry reference = ReferencesService.getReference(bibtexkey, false);
        if (reference == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .entity("No reference found with BibTeX-key: " + bibtexkey).build());
        }

        User user = (User) sc.getUserPrincipal();
        List<Map<String, String>> res = ReferencesService.getSuggestions(bibtexkey, user);
        return res;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"USER", "MAINTAINER"})
    @Path("{bibtexkey}/suggestions")
    public Response saveSuggestion(@Context SecurityContext sc, @PathParam("bibtexkey") String bibtexkey,
                                   @ApiParam(required = true) BibTeXEntry newSuggestion, @Context UriInfo uriInfo) {

        if (ReferencesService.getReference(bibtexkey, false) == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .entity("No reference found with BibTeX-key: " + bibtexkey).build());
        }

        if (newSuggestion == null || !newSuggestion.getKey().getValue().equals(bibtexkey)) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid request body").build());
        }

        // check if crossref exists
        String crossref = ReferencesService.getCrossrefKeyIfReferenceNotExists(newSuggestion);
        if (crossref != null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .entity("Crossref: No reference found with BibTeX-key: " + crossref).build());
        }

        User user = (User) sc.getUserPrincipal();

        // save suggestion
        Integer result = ReferencesService.saveReference(newSuggestion, user);

        if (result == null || result <= 0) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        // added suggestion for reference
        UriBuilder path = uriInfo.getAbsolutePathBuilder();
        path.path(Integer.toString(result));

        return Response.created(path.build()).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"MAINTAINER"})
    @Path("{bibtexkey}/suggestions/{id}")
    public Response updateSuggestion(@Context SecurityContext sc, @PathParam("bibtexkey") String bibtexkey,
                                     @PathParam("id") int id, @ApiParam(required = true) BibTeXEntry editedSuggestion,
                                     @Context UriInfo uriInfo) {

        if (editedSuggestion == null || !editedSuggestion.getKey().getValue().equals(bibtexkey)) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid request body").build());
        }

        // check if reference exists
        if (ReferencesService.getReference(bibtexkey, false) == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .entity("No reference found with BibTeX-key: " + bibtexkey).build());
        }

        // check if suggestion exists
        if (id < 1 || !GitService.existsSuggestion(bibtexkey, id)) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .entity("No suggestion with id " + id + " found for BibTeX-key: " + bibtexkey).build());
        }

        // check if crossref exists
        String crossref = ReferencesService.getCrossrefKeyIfReferenceNotExists(editedSuggestion);
        if (crossref != null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .entity("Crossref: No reference found with BibTeX-key: " + crossref).build());
        }

        User user = (User) sc.getUserPrincipal();

        if (!GitService.commitSuggestion(user, editedSuggestion, id)) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }

        // delete ratings of old suggestion
        ReferencesService.deleteRatingSuggestion(bibtexkey, id);

        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"USER", "MAINTAINER"})
    @Path("{bibtexkey}/suggestions/{id}/rating")
    public ResponseRatingSuggestion rateSuggestion(@Context SecurityContext sc, @PathParam("bibtexkey") String bibtexkey,
                                                   @PathParam("id") int id, @ApiParam(required = true) Rating rating) {

        if (rating == null || bibtexkey == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid request body").build());
        }

        Rating.RatingEnum ratingUser = rating.getUserRating();

        if (ratingUser == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid request body").build());
        }

        // check if reference exists
        if (ReferencesService.getReference(bibtexkey, false) == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .entity("No reference found with BibTeX-key: " + bibtexkey).build());
        }

        // check if suggestion exists
        if (id < 1 || !GitService.existsSuggestion(bibtexkey, id)) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .entity("No suggestion with id " + id + " found for BibTeX-key: " + bibtexkey).build());
        }

        // get user
        User user = (User) sc.getUserPrincipal();

        // save rating
        MergeInstruction.MergeEnum merged = ReferencesService.rateSuggestion(bibtexkey, id, ratingUser, user);

        // return if merged and overall rating
        ResponseRatingSuggestion response = new ResponseRatingSuggestion();
        response.setMerged(merged);
        response.setOverallRating(ReferencesService.getRatingSuggestion(bibtexkey, id));

        return response;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"MAINTAINER"})
    @Path("{bibtexkey}/suggestions/{id}/merge")
    public Response mergeSuggestion(@Context SecurityContext sc, @PathParam("bibtexkey") String bibtexkey,
                                    @PathParam("id") int id, @ApiParam(required = true) MergeInstruction mergeInstruction) {

        if (mergeInstruction == null || bibtexkey == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid request body").build());
        }

        MergeInstruction.MergeEnum mergeEnum = mergeInstruction.getMergeInstruction();

        if (mergeEnum == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid request body").build());
        }

        // check if reference exists
        if (ReferencesService.getReference(bibtexkey, false) == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .entity("No reference found with BibTeX-key: " + bibtexkey).build());
        }

        // check if suggestion exists
        if (id < 1 || !GitService.existsSuggestion(bibtexkey, id)) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .entity("No suggestion with id " + id + " found for BibTeX-key: " + bibtexkey).build());
        }

        // get user
        User user = (User) sc.getUserPrincipal();

        // save rating
        if (!ReferencesService.mergeSuggestion(bibtexkey, id, mergeEnum, user)) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }

        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
