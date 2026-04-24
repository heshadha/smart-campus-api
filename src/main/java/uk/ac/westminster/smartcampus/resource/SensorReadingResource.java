package uk.ac.westminster.smartcampus.resource;

import uk.ac.westminster.smartcampus.data.InMemoryDataStore;
import uk.ac.westminster.smartcampus.model.Sensor;
import uk.ac.westminster.smartcampus.model.SensorReading;
import uk.ac.westminster.smartcampus.exception.SensorUnavailableException;
import uk.ac.westminster.smartcampus.exception.LinkedResourceNotFoundException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class SensorReadingResource {

    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // retrieves the historical log of telemetry captured by this specific hardware unit
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<SensorReading> getReadings() {
        return InMemoryDataStore.SENSOR_READINGS.getOrDefault(sensorId, new ArrayList<>());
    }

    // appends a new reading to the sensor log and synchronizes the parent sensor state
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading) {
        Sensor parentSensor = InMemoryDataStore.SENSORS.get(sensorId);

        // validates that the hardware exists before accepting telemetry
        if (parentSensor == null) {
            throw new LinkedResourceNotFoundException("Telemetry rejected: Sensor " + sensorId + " not found");
        }

        // enforces state constraints by blocking readings from hardware under maintenance
        if ("MAINTENANCE".equalsIgnoreCase(parentSensor.getStatus())) {
            throw new SensorUnavailableException("Sensor " + sensorId + " is currently in maintenance and cannot accept readings");
        }

        // server strictly overrides client input to guarantee unique identifiers and accurate event timing
        reading.setId(java.util.UUID.randomUUID().toString());
        reading.setTimestamp(System.currentTimeMillis());

        // captures the reading into the historical record
        InMemoryDataStore.SENSOR_READINGS.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);

        // side effect: synchronizes the parent sensor's live measurement with the latest reading
        parentSensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}