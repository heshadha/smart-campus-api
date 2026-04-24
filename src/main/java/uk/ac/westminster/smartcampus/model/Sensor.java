package uk.ac.westminster.smartcampus.model;

public class Sensor {
    private String id;
    private String type;
    private String status;
    private double currentValue;
    private String roomId;

    // required empty constructor for JAX-RS JSON deserialization
    public Sensor() {
    }

    // registers a newly deployed hardware sensor linked to a specific physical space
    public Sensor(String id, String type, String status, double currentValue, String roomId) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.currentValue = currentValue;
        this.roomId = roomId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // acts as the live snapshot of the most recent telemetry measurement
    public double getCurrentValue() { return currentValue; }
    public void setCurrentValue(double currentValue) { this.currentValue = currentValue; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
}