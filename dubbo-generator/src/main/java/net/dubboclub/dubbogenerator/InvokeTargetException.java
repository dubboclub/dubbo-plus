package net.dubboclub.dubbogenerator;

/**
 * Created by bieber on 2015/7/31.
 */
public class InvokeTargetException extends RuntimeException {
    
    private Class<?> targetType;
    
    private String targetMethod;
    
    private Throwable cause;

    public InvokeTargetException(Class<?> targetType,String targetMethod,Throwable cause) {
        super(cause);
        this.targetType=targetType;
        this.targetMethod=targetMethod;
        cause=cause.getCause();
    }

    public InvokeTargetException(String message) {
        super(message);
    }

    public InvokeTargetException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvokeTargetException(Throwable cause) {
        super(cause);
    }

    public Class<?> getTargetType() {
        return targetType;
    }

    public String getTargetMethod() {
        return targetMethod;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }
}
