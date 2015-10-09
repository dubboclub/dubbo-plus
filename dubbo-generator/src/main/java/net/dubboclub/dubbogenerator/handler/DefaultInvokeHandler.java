package net.dubboclub.dubbogenerator.handler;

/**
 * Created by bieber on 2015/10/8.
 * 默认请求拦截器,只进行日志的记录
 */
public class DefaultInvokeHandler extends AbstractHandler {
    
    @Override
    public void beforeInvoke(Class<?> clientType, String methodName, Object[] args) {
        LOGGER.info("invoke service [{}] method [{}] args [{}]",clientType.getName(),methodName,parseArgsToJson(args));
    }

    @Override
    public void completeInvoke(Class<?> clientType, String methodName, Object ret, Object[] args) {
        LOGGER.info("invoker service [{}] method [{}] args[{}] return [{}]",clientType.getName(),methodName,parseArgsToJson(args),parseObject2Json(ret));
    }

    @Override
    public void caughtException(Class<?> clientType, String methodName, Throwable e, Object[] args) {
        LOGGER.error("invoke service [{}] method [{}]  args [{}] ,occur an exception ", clientType.getName(), methodName, parseArgsToJson(args));
        LOGGER.error("case by exception :",e);
    }
}
