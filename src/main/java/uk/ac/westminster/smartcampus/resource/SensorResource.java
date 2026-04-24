package uk.ac.westminster.smartcampus.resource;

import uk.ac.westminster.smartcampus.data.InMemoryDataStore;
import uk.ac.westminster.smartcampus.model.ErrorMessage;
import uk.ac.westminster.smartcampus.model.Room;
import uk.ac.westminster.smartcampus.model.Sensor;
import uk.ac.westminster.smartcampus.exception.LinkedResourceNotFoundException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    // retrieves all registered sensors or filters by category if the type parameter is provided
    @GET
    public Collection<Sensor> getSensors(@QueryParam("type") String type) {
        if (type == null || type.isEmpty()) {
            return InMemoryDataStore.SENSORS.values();
        }

        // applies a filter to the sensor stream based on the hardware category
        return InMemoryDataStore.SENSORS.values().stream()
                .filter(s -> s.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }
    
    // allows users to fetch metadata for a specific sensor unit
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = InMemoryDataStore.SENSORS.get(sensorId);
        
        if (sensor == null) {
            // returns a 404 if the requested infrastructure component is not found
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        return Response.ok(sensor).build();
    }

    // registers new telemetry hardware and validates the target room assignment
    @POST
    public Response addSensor(Sensor sensor) {
        // validates if the client provided id already exists to prevent overwriting
        if (sensor.getId() != null && InMemoryDataStore.SENSORS.containsKey(sensor.getId())) {
            ErrorMessage error = new ErrorMessage("Sensor ID " + sensor.getId() + " already exists", Response.Status.BAD_REQUEST.getStatusCode());
            return Response.status(Response.Status.BAD_REQUEST).entity(error).build();
        }

        // verifies that the specified room exists in the campus registry
        Room targetRoom = InMemoryDataStore.ROOMS.get(sensor.getRoomId());

        if (targetRoom == null) {
            throw new LinkedResourceNotFoundException("Cannot register sensor " + sensor.getId() 
                + " because room " + sensor.getRoomId() + " does not exist");
        }

        // commits the sensor to the store and updates the room's internal sensor list
        InMemoryDataStore.SENSORS.put(sensor.getId(), sensor);
        targetRoom.getSensorIds().add(sensor.getId());

        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }
    
    // hands off control to a specialized resource for managing nested reading telemetry
    @Path("{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}