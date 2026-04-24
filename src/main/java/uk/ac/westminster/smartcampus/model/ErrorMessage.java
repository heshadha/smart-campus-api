package uk.ac.westminster.smartcampus.model;

public class ErrorMessage {
    private String error;
    private int status;

    public ErrorMessage() {
    }

    public ErrorMessage(String error, int status) {
        this.error = error;
        this.status = status;
    }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
}