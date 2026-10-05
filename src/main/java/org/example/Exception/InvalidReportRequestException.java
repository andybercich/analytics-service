package org.example.Exception;

public class InvalidReportRequestException extends RuntimeException {

    public InvalidReportRequestException(String message) {
        super(message);
    }
}
