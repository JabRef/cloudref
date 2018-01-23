package eu.cloudref;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import eu.cloudref.auth.CloudRefDynamicFeature;
import eu.cloudref.rest.*;
import io.swagger.jaxrs.config.BeanConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

@ApplicationPath("/")
public class CloudRefApi extends Application {

    public CloudRefApi() {
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0");
        beanConfig.setSchemes(new String[]{"http", "http","https"});
        beanConfig.setDescription("REST API of the CloudRef system.");
        beanConfig.setTitle("CloudRef API");
        beanConfig.setBasePath("");
        beanConfig.setResourcePackage("eu.cloudref");
        beanConfig.setScan(true);
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new HashSet<>();

        resources.add(CORSResponseFilter.class);
        resources.add(MultiPartFeature.class);
        resources.add(JacksonFeature.class);
        resources.add(CloudRefDynamicFeature.class);

        // REST classes
        resources.add(Comment.class);
        resources.add(PdfFile.class);
        resources.add(Reference.class);
        resources.add(Suggestion.class);
        resources.add(User.class);

        resources.add(io.swagger.jaxrs.listing.ApiListingResource.class);
        resources.add(io.swagger.jaxrs.listing.SwaggerSerializers.class);

        return resources;
    }
}
