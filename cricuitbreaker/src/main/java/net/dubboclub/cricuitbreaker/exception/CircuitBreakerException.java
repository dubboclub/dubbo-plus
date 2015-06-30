package net.dubboclub.cricuitbreaker.exception;

/**
 * Created by bieber on 2015/6/3.
 */
public class CircuitBreakerException extends Exception {
    
    private String code;
    
    private String message;
    
    public CircuitBreakerException(String code, String message){
        this.code=code;
        this.message = message;
    }
    

    protected CircuitBreakerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public CircuitBreakerException() {
        super();
    }

    public CircuitBreakerException(String message) {
        super(message);
    }

    public CircuitBreakerException(String message, Throwable cause) {
        super(message, cause);
    }

    public CircuitBreakerException(Throwable cause) {
        super(cause);
    }
}
