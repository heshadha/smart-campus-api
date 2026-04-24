package uk.ac.westminster.smartcampus.exception;

import uk.ac.westminster.smartcampus.model.ErrorMessage;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RoomNotEmptyMapper implements ExceptionMapper<RoomNotEmptyException> {
    @Override
    public Response toResponse(RoomNotEmptyException ex) {
        // maps to 409 conflict when room deletion is prevented by assigned sensors
        ErrorMessage error = new ErrorMessage(ex.getMessage(), Response.Status.CONFLICT.getStatusCode());
        return Response.status(Response.Status.CONFLICT).entity(error).build();
    }
}