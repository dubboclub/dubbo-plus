package net.dubboclub.akka.core.exception;

/**
 * Created by bieber on 2015/7/8.
 */
public class AkkaSystemException extends RuntimeException {

    public AkkaSystemException() {
    }

    public AkkaSystemException(String message) {
        super(message);
    }

    public AkkaSystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public AkkaSystemException(Throwable cause) {
        super(cause);
    }

    public AkkaSystemException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
