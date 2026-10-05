package org.example.Exception;

public class KafkaListenerException extends RuntimeException {

    public KafkaListenerException(String message) {
        super(message);
    }

    public KafkaListenerException(String message, Throwable cause) {
        super(message, cause);
    }
}
