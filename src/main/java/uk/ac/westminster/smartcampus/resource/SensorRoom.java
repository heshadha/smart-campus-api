package uk.ac.westminster.smartcampus.resource;

import uk.ac.westminster.smartcampus.data.InMemoryDataStore;
import uk.ac.westminster.smartcampus.model.ErrorMessage;
import uk.ac.westminster.smartcampus.model.Room;
import uk.ac.westminster.smartcampus.exception.RoomNotEmptyException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorRoom {

    // fetches the current state of all campus rooms within the registry
    @GET
    public Collection<Room> getAllRooms() {
        return InMemoryDataStore.ROOMS.values();
    }

    // registers a new physical room into the campus infrastructure
    @POST
    public Response createRoom(Room room) {
        // validates if the client provided id already exists to prevent overwriting
        if (room.getId() != null && InMemoryDataStore.ROOMS.containsKey(room.getId())) {
            ErrorMessage error = new ErrorMessage("Room ID " + room.getId() + " already exists", Response.Status.BAD_REQUEST.getStatusCode());
            // using a direct 400 bad request response here as it is a client error, preventing it from triggering the global 500 catch-all
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        InMemoryDataStore.ROOMS.put(room.getId(), room);
        return Response.status(Response.Status.CREATED).entity(room).build();
    }

    // retrieves detailed metadata for a specific room identifier
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = InMemoryDataStore.ROOMS.get(roomId);
        if (room == null) {
            // returns a 404 if the requested infrastructure component is not found
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(room).build();
    }

    // decommissions a room from the system if no active hardware is present
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = InMemoryDataStore.ROOMS.get(roomId);

        // returns success immediately if the resource already does not exist
        if (room == null) {
            return Response.noContent().build();
        }

        // enforces referential integrity by blocking deletion of occupied rooms
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Cannot delete room " + roomId + " because it still contains active sensors");
        }

        // removes the room from the infrastructure registry
        InMemoryDataStore.ROOMS.remove(roomId);
        
        return Response.noContent().build();
    }
}