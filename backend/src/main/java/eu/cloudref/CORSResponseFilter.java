package eu.cloudref;

import javax.ws.rs.ext.Provider;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

@Provider
public class CORSResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext creq, ContainerResponseContext cres) {
        // development: localhost:4200 - however, we might be hosted anywhere - quick hack
        cres.getHeaders().add("Access-Control-Allow-Origin", "*");

        cres.getHeaders().add("Access-Control-Allow-Headers", "Authorization, Accept, Origin, X-Requested-With, Content-Type, Last-Modified");
        cres.getHeaders().add("Access-Control-Allow-Credentials", "true");
        cres.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, HEAD, PUT, DELETE, OPTIONS");
    }

}
