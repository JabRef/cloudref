package eu.cloudref.rest;

import eu.cloudref.dal.PdfService;
import eu.cloudref.dal.ReferencesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.InputStream;

@Api
@Path("references")
public class PdfFile {

    @GET
    @RolesAllowed({"USER", "MAINTAINER"})
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("{bibtexkey}/pdf")
    public Response getPdfFile(@PathParam("bibtexkey") String bibtexkey) {

        if (ReferencesService.getReference(bibtexkey, false) == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .entity("No reference found with BibTeX-key: " + bibtexkey).build());
        }
        File file = PdfService.getPdfFile(bibtexkey);

        if (file == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .entity("No PDF file found for reference with BibTeX-key: " + bibtexkey).build());
        }

        return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"" )
                .build();
    }

    @PUT
    @RolesAllowed({"USER", "MAINTAINER"})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("{bibtexkey}/pdf")
    public Response savePdfFile(@PathParam("bibtexkey") String bibtexkey,
                                @ApiParam(required = true) @FormDataParam("file") InputStream uploadedInputStream,
                                @FormDataParam("file") FormDataContentDisposition fileDetail) {

        if (ReferencesService.getReference(bibtexkey, false) == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                    .entity("No reference found with BibTeX-key: " + bibtexkey).build());
        }

        if (uploadedInputStream == null || fileDetail == null) {
            throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid request body").build());
        }

        if (!PdfService.savePdfFile(bibtexkey, uploadedInputStream)) {
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        }

        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
