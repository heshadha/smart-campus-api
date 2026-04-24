package uk.ac.westminster.smartcampus.data;

import uk.ac.westminster.smartcampus.model.Room;
import uk.ac.westminster.smartcampus.model.Sensor;
import uk.ac.westminster.smartcampus.model.SensorReading;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryDataStore {

    // concurrent collections ensure thread-safe data access across simultaneous api requests
    public static final Map<String, Room> ROOMS = new ConcurrentHashMap<>();
    public static final Map<String, Sensor> SENSORS = new ConcurrentHashMap<>();
    public static final Map<String, List<SensorReading>> SENSOR_READINGS = new ConcurrentHashMap<>();

    // atomic counters to generate non-uuid readable ids for physical assets when clients do not provide them
    public static final AtomicInteger ROOM_COUNTER = new AtomicInteger(1);
    public static final AtomicInteger SENSOR_COUNTER = new AtomicInteger(1);

    static {
        // initialize baseline infrastructure topology for system startup
        
        // --- room 1: library ---
        Room libRoom = new Room("LIB-301", "Library Quiet Study", 50);
        ROOMS.put(libRoom.getId(), libRoom);
        
        Sensor tempSensor1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, libRoom.getId());
        SENSORS.put(tempSensor1.getId(), tempSensor1);
        libRoom.getSensorIds().add(tempSensor1.getId());

        Sensor co2Sensor = new Sensor("CO2-001", "CO2", "ACTIVE", 400.0, libRoom.getId());
        SENSORS.put(co2Sensor.getId(), co2Sensor);
        libRoom.getSensorIds().add(co2Sensor.getId());

        // --- room 2: computer science lab ---
        Room labRoom = new Room("LAB-101", "Computer Science Lab", 30);
        ROOMS.put(labRoom.getId(), labRoom);

        // this sensor is explicitly set to maintenance to allow testing of the 403 forbidden error mapping
        Sensor tempSensor2 = new Sensor("TEMP-002", "Temperature", "MAINTENANCE", 0.0, labRoom.getId());
        SENSORS.put(tempSensor2.getId(), tempSensor2);
        labRoom.getSensorIds().add(tempSensor2.getId());

        // generates standard uuids for initial readings as recommended by specifications
        // using copyonwritearraylist ensures thread-safe appending of historical telemetry data
        List<SensorReading> libTempReadings = new CopyOnWriteArrayList<>();
        libTempReadings.add(new SensorReading(UUID.randomUUID().toString(), System.currentTimeMillis() - 60000, 22.0));
        libTempReadings.add(new SensorReading(UUID.randomUUID().toString(), System.currentTimeMillis(), 22.5));
        SENSOR_READINGS.put(tempSensor1.getId(), libTempReadings);

        List<SensorReading> libCo2Readings = new CopyOnWriteArrayList<>();
        libCo2Readings.add(new SensorReading(UUID.randomUUID().toString(), System.currentTimeMillis(), 400.0));
        SENSOR_READINGS.put(co2Sensor.getId(), libCo2Readings);
        
        // initializes an empty thread-safe log for the maintenance sensor
        SENSOR_READINGS.put(tempSensor2.getId(), new CopyOnWriteArrayList<>());
    }
}