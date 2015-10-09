package net.dubboclub.dubbogenerator.handler;

/**
 * Created by bieber on 2015/7/31.
 */
public interface InvokeHandler {

    public static final String LOGGER_NAME="DUBBO_INTEGRATION";

    public void beforeInvoke(Class<?> clientType, String methodName, Object[] args);

    public void completeInvoke(Class<?> clientType, String methodName, Object ret, Object[] args);
    
    public void caughtException(Class<?> clientType, String methodName, Throwable e, Object[] args);
    
}
