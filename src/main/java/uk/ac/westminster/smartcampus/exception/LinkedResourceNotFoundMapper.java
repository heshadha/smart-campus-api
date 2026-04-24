package uk.ac.westminster.smartcampus.exception;

import uk.ac.westminster.smartcampus.model.ErrorMessage;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class LinkedResourceNotFoundMapper implements ExceptionMapper<LinkedResourceNotFoundException> {
    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {
        // maps to 422 unprocessable entity for invalid relational data links
        ErrorMessage error = new ErrorMessage(ex.getMessage(), 422);
        return Response.status(422).entity(error).build();
    }
}