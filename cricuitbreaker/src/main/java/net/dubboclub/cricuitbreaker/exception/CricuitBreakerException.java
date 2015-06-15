package net.dubboclub.cricuitbreaker.exception;

/**
 * Created by bieber on 2015/6/3.
 */
public class CricuitBreakerException extends Exception {
    
    private String code;
    
    private String message;
    
    public CricuitBreakerException(String code,String message){
        this.code=code;
        this.message = message;
    }
    

    protected CricuitBreakerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public CricuitBreakerException() {
        super();
    }

    public CricuitBreakerException(String message) {
        super(message);
    }

    public CricuitBreakerException(String message, Throwable cause) {
        super(message, cause);
    }

    public CricuitBreakerException(Throwable cause) {
        super(cause);
    }
}
