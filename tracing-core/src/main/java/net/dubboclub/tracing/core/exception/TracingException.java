package net.dubboclub.tracing.core.exception;

/**
 * TracingException
 * Created by bieber.bibo on 16/11/3
 */

public class TracingException extends RuntimeException {

    public TracingException() {
        super();
    }

    public TracingException(Throwable cause) {
        super(cause);
    }

    public TracingException(String message) {
        super(message);
    }

    public TracingException(String message, Throwable cause) {
        super(message, cause);
    }

    protected TracingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
