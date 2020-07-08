package org.imenik.web.exception;

public class NullContactException extends RuntimeException {
    public NullContactException(String message) {
        super(message);
    }

    public NullContactException(String message, Throwable cause) {
        super(message, cause);
    }
}
