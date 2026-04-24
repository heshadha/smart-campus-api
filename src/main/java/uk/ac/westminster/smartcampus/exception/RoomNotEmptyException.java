package uk.ac.westminster.smartcampus.exception;

// thrown when a room decommissioning is blocked by active hardware presence
public class RoomNotEmptyException extends RuntimeException {
    public RoomNotEmptyException(String message) {
        super(message);
    }
}