package uk.ac.westminster.smartcampus.exception;

// thrown when telemetry is rejected due to the sensor's current maintenance state
public class SensorUnavailableException extends RuntimeException {
    public SensorUnavailableException(String message) {
        super(message);
    }
}