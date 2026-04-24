package uk.ac.westminster.smartcampus.exception;

import uk.ac.westminster.smartcampus.model.ErrorMessage;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        // default to 500 for unknown code crashes
        int status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        String message = "An unexpected internal server error occurred within the campus network";

        if (exception instanceof WebApplicationException) {
            // captures the intended status code (e.g., 404 or 405) from the framework
            status = ((WebApplicationException) exception).getResponse().getStatus();
            message = exception.getMessage();
        } else {
            // logs severe system failures that are not standard web errors
            LOGGER.log(Level.SEVERE, "Unhandled system infrastructure failure", exception);
        }

        // forces the response to be JSON to prevent the server from sending default HTML
        ErrorMessage error = new ErrorMessage(message, status);

        return Response.status(status)
                .type(MediaType.APPLICATION_JSON) // explicitly force the content type
                .entity(error)
                .build();
    }
}