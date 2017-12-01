package eu.cloudref.rest;

import eu.cloudref.dal.ReferencesService;
import eu.cloudref.db.User;
import eu.cloudref.models.Rating;
import eu.cloudref.models.ResponseRatingReference;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;
import org.jbibtex.StringValue;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Api
@Path("references")
public class Reference {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"USER", "MAINTAINER"})
    @Path("{bibtexkey}")
    public BibTeXEntry getReference(@PathParam("bibtexkey") String bibtexkey, @Context SecurityContext sc) {

        BibTeXEntry reference = ReferencesService.getReference(bibtexkey, true);
        if (reference == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .entity("No reference found with BibTeX-key: " + bibtexkey).build());
        }

        User user = (User) sc.getUserPrincipal();
        String[] ratings = ReferencesService.getRatingsReference(bibtexkey, user.getName());
        if (ratings != null && ratings.length == 3) {
            if (ratings[0] != null && ratings[0] != null) {
                // get confirmation
                reference.addField(new Key("Confirmed"), new StringValue(ratings[0], StringValue.Style.BRACED));
            }
            if (ratings[1] != null && ratings[1] != null) {
                // get overall rating
                reference.addField(new Key("OverallRating"), new StringValue(ratings[1], StringValue.Style.BRACED));
            }
            if (ratings[2] != null && ratings[2] != null) {
                // get user rating
                reference.addField(new Key("RatedByUser"), new StringValue(ratings[2], StringValue.Style.BRACED));
            }
        }

        return reference;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({"USER", "MAINTAINER"})
    public List<eu.cloudref.models.Reference> getReferences() {

        List<BibTeXEntry> entries = ReferencesService.getReferences();
        List<eu.cloudref.models.Reference> references = new ArrayList<>();

        for (BibTeXEntry entry : entries) {
            eu.cloudref.models.Reference r = new eu.cloudref.models.Reference(entry.getKey().getValue(), entry.getType().getValue());

            if (entry.getField(BibTeXEntry.KEY_TITLE) != null) {
                r.setTitle(entry.getField(BibTeXEntry.KEY_TITLE).toUserString());
            } else {
                r.setTitle("");
            }
            String pdf = entry.getField(new Key("Pdf")).toUserString();

            if (pdf.equals("true")) {
                r.setPdf(true);
            } else {
                r.setPdf(false);
            }
            if (entry.getField(BibTeXEntry.KEY_AUTHOR) != null) {
                r.setAuthor(entry.getField(BibTeXEntry.KEY_AUTHOR).toUserString());
            }
            if (entry.getField(BibTeXEntry.KEY_EDITOR) != null) {
                r.setEditor(entry.getField(BibTeXEntry.KEY_EDITOR).toUserString());
            }
            if (entry.getField(BibTeXEntry.KEY_YEAR) != null) {
                if (!entry.getField(BibTeXEntry.KEY_YEAR).toUserString().equals("")) {
                    r.setYear(Integer.valueOf(entry.getField(BibTeXEntry.KEY_YEAR).toUserString()));
                }
            }
            if (entry.getField(BibTeXEntry.KEY_JOURNAL) != null) {
                r.setJournal(entry.getField(BibTeXEntry.KEY_JOURNAL).toUserString());
            }
            if (entry.getField(BibTeXEntry.KEY_BOOKTITLE) != null) {
                r.setBooktitle(entry.getField(BibTeXEntry.KEY_BOOKTITLE).toUserString());
            }
            // check if reference is confirmed by the users
            boolean c = ReferencesService.isConfirmed(r.getBibtexkey());
            r.setConfirmed(c);

            references.add(r);
        }

        return references;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"USER", "MAINTAINER"})
    @Path("{bibtexkey}")
    public Response saveReference(@Context SecurityContext sc, @PathParam("bibtexkey") String bibtexkey,
                                  @ApiParam(required = true) BibTeXEntry newReference, @Context UriInfo uriInfo) {

        if (ReferencesService.getReference(bibtexkey, false) != null) {
            throw new WebApplicationException(Response.status(Response.Status.CONFLICT)
                    .entity("Reference with the same BibTeX-key already exists").build());
        }

        if (newReference == null || !newReference.getKey().getValue().equals(bibtexkey)) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid request body").build());
        }

        // check if crossref exists
        String crossref = ReferencesService.getCrossrefKeyIfReferenceNotExists(newReference);
        if (crossref != null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .entity("Crossref: No reference found with BibTeX-key: " + crossref).build());
        }

        User user = (User) sc.getUserPrincipal();

        // save new reference
        Integer result = ReferencesService.saveReference(newReference, user);

        if (result == null || result != 0) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        // added new reference
        UriBuilder path = uriInfo.getAbsolutePathBuilder();
        return Response.created(path.build()).build();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RolesAllowed({"USER", "MAINTAINER"})
    public Response saveReferences(@Context SecurityContext sc, @Context UriInfo uriInfo,
                                   @ApiParam(required = true) @FormDataParam("file") InputStream uploadedInputStream,
                                   @FormDataParam("file") FormDataContentDisposition fileDetail) {

        if (uploadedInputStream == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid request body").build());
        }

        User user = (User) sc.getUserPrincipal();

        if (!ReferencesService.saveReferences(uploadedInputStream, user)) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid request body").build());
        }

        UriBuilder path = uriInfo.getAbsolutePathBuilder();
        return Response.created(path.build()).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @RolesAllowed({"USER", "MAINTAINER"})
    @Path("{bibtexkey}/rating")
    public ResponseRatingReference rateReference(@Context SecurityContext sc, @PathParam("bibtexkey") String bibtexkey,
                                                 @ApiParam(required = true) Rating rating) {

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

        // get user
        User user = (User) sc.getUserPrincipal();

        // save rating of reference
        ReferencesService.rateReference(bibtexkey, ratingUser, user);

        // return if confirmed and overall rating
        ResponseRatingReference response = new ResponseRatingReference();
        response.setConfirmed(ReferencesService.isConfirmed(bibtexkey));
        response.setOverallRating(ReferencesService.getRatingReference(bibtexkey));

        return response;
    }
}
