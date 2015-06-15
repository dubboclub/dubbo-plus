package com.dubboclub.cricuitbreaker.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.rpc.*;
import com.dubboclub.cricuitbreaker.exception.CricuitBreakerException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by bieber on 2015/4/30.
 * Dubbo服务降级Filter
 * 通过拦截每个方法的请求，并且读取每个方法对服务降级条件的配置
 * 从而自动进行服务降级，以及服务恢复
 */
@Activate(group = {Constants.CONSUMER})
public class RemoteFacadeCircuitBreaker implements Filter {
    

    private static final Logger logger = LoggerFactory.getLogger(RemoteFacadeCircuitBreaker.class);

    //用于存储某个方法出现异常的计数器key:interfaceName.methodName,value:对应的异常计数器
    private volatile ConcurrentHashMap<String,BreakCounter> breakCounterMap = new ConcurrentHashMap<String,BreakCounter>();
    //对某个方法异常计数器处理器，用于分析记录的异常到当前实现是否失效了（为了满足在某个时间内出现异常次数），如果失效将从BreakCounter中移除
    private BreakCounterLoop[] breakCounterLoops = new BreakCounterLoop[Runtime.getRuntime().availableProcessors()];
    //获取dubbo的代理工程扩展
    private final static ProxyFactory proxyFactory = ExtensionLoader.getExtensionLoader(ProxyFactory.class).getAdaptiveExtension();
    //缓存服务降级代理类的Invoker，避免重复创建
    private final static ConcurrentHashMap<String,Invoker> CIRCUIT_BREAKER_INVOKER_CACHE = new ConcurrentHashMap<String, Invoker>();
    
    private volatile AtomicLong loopCount = new AtomicLong(0);

    public RemoteFacadeCircuitBreaker(){
        String intervalConf = ConfigUtils.getProperty("dubbo.reference.check.break.marker.interval", "60000");
        logger.debug("has already been initialized circuit breaker,check break marker interval ["+intervalConf+"]");
        long interval = Long.parseLong(intervalConf);
        for(int i=0;i<breakCounterLoops.length;i++){
            BreakCounterLoop loop = new BreakCounterLoop(interval);
            breakCounterLoops[i]=loop;
        }
    }

    //获取下一个遍历器
    private BreakCounterLoop nextLoop(){
        return breakCounterLoops[((int) (loopCount.incrementAndGet() % breakCounterLoops.length))];
    }

    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if(Config.checkFunctionSwitch(invoker, invocation)){
            return wrapBreakerInvoke(invoker,invocation);
        }
        Result result =  invoker.invoke(invocation);
        toBeNormal(invoker,invocation);
        return result;
    }



    
    private Result wrapBreakerInvoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        //首先检查是否需要进入服务降级流程
        if(checkNeedCircuitBreak(invoker, invocation)){
            if(logger.isDebugEnabled()){
                logger.debug("activate the circuit break for url ["+invoker.getUrl()+"],invoke method ["+invocation.getMethodName()+"]");
            }
            //进入服务降级
            return doCircuitBreak(invoker, invocation);
        }
        try{
            Result result = invoker.invoke(invocation);
            //将该服务从服务降级中恢复出来
            toBeNormal(invoker, invocation);
            return result;
        }catch (RpcException e){
            //如果是业务异常，则直接返回异常
            if(e.isBiz()){
                throw e;
            }
            caughtException(invoker,invocation,e);
            throw e;
        }
    }

    /**
     * 将服务恢复正常
     * @param invoker
     * @param invocation
     */
    private void toBeNormal(Invoker<?> invoker, Invocation invocation){
        String interfaceName = invoker.getUrl().getParameter(Constants.INTERFACE_KEY);
        String method = invocation.getMethodName();
        StringBuffer methodConfig = new StringBuffer(Config.DUBBO_REFERENCE_PREFIX);
        methodConfig.append(interfaceName).append(".").append(method);
        String methodKey = methodConfig.toString();
        //从其中删除对应的异常计数器
        BreakCounter counter = breakCounterMap.remove(methodKey);
        if(counter!=null){
            //将这个counter设置为失效
            counter.disable();
        }
    }


    /**
     * 这里会判断当前调用服务的状态，分析判断是否需要进入降级状态
     * 如果服务在指定的时间区间内累积的错误，达到了配置的次数，则进入服务降级
     * 如果满足上面条件，并且满足重试机制，则也不会进入降级流程，而是触发远程服务调用
     * @param invoker
     * @param invocation
     * @return
     */
    private boolean checkNeedCircuitBreak(Invoker<?> invoker, Invocation invocation) {
        String interfaceName = invoker.getUrl().getParameter(Constants.INTERFACE_KEY);
        String method = invocation.getMethodName();
        String methodKey = Config.getMethodPropertyName(invoker, invocation).toString();
        int limit = Config.getBreakLimit(invoker, invocation);
        BreakCounter breakCounter = breakCounterMap.get(methodKey);
        if(breakCounter!=null&&breakCounter.enable()){
            long currentExceptionCount = breakCounter.getCurrentExceptionCount();
            long currentBreakCount = breakCounter.getCurrentBreakCount();
            if(logger.isDebugEnabled()){
                logger.debug("check invoke "+interfaceName+"."+method+"() circuit break,current break count ["+currentBreakCount+"],the limit is ["+limit+"]");
            }
            if(limit<=currentExceptionCount){
                if(currentBreakCount>0&&needRetry(invoker, invocation, currentBreakCount)){
                    breakCounter.incrementBreakCount();
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    private boolean needRetry(Invoker<?> invoker, Invocation invocation, long currentBreakCount){
        String interfaceName = invoker.getUrl().getParameter(Constants.INTERFACE_KEY);
        String method = invocation.getMethodName();
        int frequency = Config.getRetryFrequency(invoker, invocation);
        if(logger.isDebugEnabled()){
            logger.debug("check invoke "+interfaceName+"."+method+"() need retry,current break count ["+currentBreakCount+"],retry frequency ["+frequency+"]");
        }
        if(currentBreakCount%frequency==0){
            if(logger.isInfoEnabled()){
                logger.info("retry invoke "+interfaceName+"."+method+"()");
            }
            return true;
        }
        return false;
    }


    private <T extends Object> Result doCircuitBreak(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String interfaceName = invoker.getUrl().getParameter(Constants.INTERFACE_KEY);
        String circuitBreaker = interfaceName+"CircuitBreak";
        incrementBreakCount(invoker, invocation);
        try {
            if(logger.isDebugEnabled()){
                logger.debug("check has class "+circuitBreaker+" to handle circuit break");
            }
            Invoker<?> breakerInvoker =null;
            if(CIRCUIT_BREAKER_INVOKER_CACHE.contains(circuitBreaker)){
                breakerInvoker = CIRCUIT_BREAKER_INVOKER_CACHE.get(circuitBreaker);
            }else{
                Class<T> breakerType = (Class<T>) Class.forName(circuitBreaker);
                Class<T> interfaceType = (Class<T>) Class.forName(interfaceName);
                if(interfaceType.isAssignableFrom(breakerType)){
                    if(logger.isDebugEnabled()){
                        logger.debug("handle circuit break by class "+circuitBreaker);
                    }

                    T breaker =  breakerType.newInstance();
                    breakerInvoker = proxyFactory.getInvoker(breaker, interfaceType, invoker.getUrl());
                    Invoker<?> oldInvoker = CIRCUIT_BREAKER_INVOKER_CACHE.putIfAbsent(circuitBreaker, breakerInvoker);
                    if(oldInvoker!=null){
                        breakerInvoker=oldInvoker;
                    }
                }
            }
            if(breakerInvoker!=null){
                return breakerInvoker.invoke(invocation);
            }
        } catch (Exception e) {
            logger.error("failed to invoke circuit breaker",e);
        }
        if(logger.isDebugEnabled()){
            logger.debug("handle circuit break by exception");
        }
        CricuitBreakerException baseBusinessException = new CricuitBreakerException(Config.DEFAULT_CIRCUIT_BREAKER_ERROR_CODE,"哎哟，系统异常，请稍后再试");
        RpcException rpcException = new RpcException(RpcException.BIZ_EXCEPTION,baseBusinessException);
        throw rpcException;
    }

    private void incrementBreakCount(Invoker<?> invoker,Invocation invocation){
        String interfaceName = invoker.getUrl().getParameter(Constants.INTERFACE_KEY);
        String method = invocation.getMethodName();
        StringBuffer interfaceConfig = new StringBuffer(Config.DUBBO_REFERENCE_PREFIX);
        interfaceConfig.append(interfaceName);
        StringBuffer methodConfig = new StringBuffer(interfaceConfig.toString());
        methodConfig.append(".").append(method);
        String methodKey = methodConfig.toString();
        BreakCounter counter =  breakCounterMap.get(methodKey);
        counter.incrementBreakCount();
    }
    
    private void caughtException(Invoker<?> invoker,Invocation invocation,Exception e){
        String interfaceName = invoker.getUrl().getParameter(Constants.INTERFACE_KEY);
        String method = invocation.getMethodName();
        StringBuffer interfaceConfig = new StringBuffer(Config.DUBBO_REFERENCE_PREFIX);
        interfaceConfig.append(interfaceName);
        StringBuffer methodConfig = new StringBuffer(interfaceConfig.toString());
        methodConfig.append(".").append(method);
        String methodKey = methodConfig.toString();
        int timeout = invoker.getUrl().getMethodParameter(invocation.getMethodName(),Constants.TIMEOUT_KEY,Constants.DEFAULT_TIMEOUT);
        int limit = Config.getBreakLimit(invoker,invocation);
        //一个异常的有效期，是通过连续出现异常数量乘以每个调用的超时时间，比如你配置连续出现10次异常之后进行服务降级，并且每次服务调用的超时事件是2000ms的话，同时
        //每个服务重试次数是为2次，那么就是在(2+1)*2000*10
        ExceptionMarker breakMarker = new ExceptionMarker(System.currentTimeMillis(), limit*timeout,e);
        if(!breakCounterMap.containsKey(methodKey)){
            BreakCounter oldValue = breakCounterMap.putIfAbsent(methodKey, new BreakCounter(methodKey));
            //返回的oldValue为空，表示之前没有创建了赌赢的异常计数器,则需要对它分配一个loop
            if(oldValue==null){
                nextLoop().register(breakCounterMap.get(methodKey));
            }
        }
        BreakCounter counter=breakCounterMap.get(methodKey);
        counter.addExceptionMarker(breakMarker);
        if(logger.isDebugEnabled()){
            logger.debug("caught exception for rpc invoke "+interfaceName+"."+method+"()，current break count ["+counter.getCurrentExceptionCount()+"]");
        }
    }
}
