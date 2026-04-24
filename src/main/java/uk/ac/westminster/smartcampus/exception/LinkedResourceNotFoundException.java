package uk.ac.westminster.smartcampus.exception;

// thrown when a provided foreign key reference does not exist in the infrastructure registry
public class LinkedResourceNotFoundException extends RuntimeException {
    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}