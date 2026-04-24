package uk.ac.westminster.smartcampus.resource;

import uk.ac.westminster.smartcampus.model.ApiMetadata;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ApiMetadata getSystemMetadata() {
        // initialize the campus network metadata
        ApiMetadata metadata = new ApiMetadata("v1.0.0", "admin@smartcampus.westminster.ac.uk");
        
        // add the HATEOAS routing map for client navigation
        metadata.addLink("self", "/api/v1/");
        metadata.addLink("rooms", "/api/v1/rooms");
        metadata.addLink("sensors", "/api/v1/sensors");
        
        return metadata;
    }
}