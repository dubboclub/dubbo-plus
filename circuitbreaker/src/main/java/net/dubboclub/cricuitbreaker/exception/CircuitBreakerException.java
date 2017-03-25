package net.dubboclub.circuitbreaker.exception;

/**
 * Created by bieber on 2015/6/3.
 */
public class CircuitBreakerException extends RuntimeException {
    
   private String message;


    public CircuitBreakerException(String interfaceName, String methodName) {
        StringBuilder message = new StringBuilder();
        message.append(interfaceName).append(".").append(methodName).append("进入默认服务降级方案.");
        this.message = message.toString();
    }


    public CircuitBreakerException(String message) {
        super(message);
    }

    @Override
    public String getMessage() {
        return message;
    }
}
