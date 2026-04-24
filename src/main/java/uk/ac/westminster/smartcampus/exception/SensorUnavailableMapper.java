package uk.ac.westminster.smartcampus.exception;

import uk.ac.westminster.smartcampus.model.ErrorMessage;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class SensorUnavailableMapper implements ExceptionMapper<SensorUnavailableException> {
    @Override
    public Response toResponse(SensorUnavailableException ex) {
        // maps to 403 forbidden when sensor state prohibits data ingestion
        ErrorMessage error = new ErrorMessage(ex.getMessage(), Response.Status.FORBIDDEN.getStatusCode());
        return Response.status(Response.Status.FORBIDDEN).entity(error).build();
    }
}