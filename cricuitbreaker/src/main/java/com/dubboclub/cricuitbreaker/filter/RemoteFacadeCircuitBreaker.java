package com.dubboclub.cricuitbreaker.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
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
    
    private static final String DEFAULT_CIRCUIT_BREAKER_ERROR_CODE="";
    
    //默认情况下1分钟内出现10个异常，同时默认情况下当满足1分钟内10个异常，然后每发生10次错误就会进行一次重试，检查服务是否恢复
    private static final String DEFAULT_BREAK_LIMIT="10";

    private static final String DEFAULT_RETRY_FREQUENCY="10";

    private static final String DEFAULT_TIME_INTERVAL="60000";

    private static final String DUBBO_REFERENCE_PREFIX="dubbo.reference.";
    
    private static final String DUBBO_REFERENCE_CIRCUIT_BREAKER_SWITCH=DUBBO_REFERENCE_PREFIX+"circuit.breaker";

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
        if(getFunctionSwitch()){
            return wrapBreakerInvoke(invoker,invocation);
        }
        return invoker.invoke(invocation);
    }
    
    
    private Result wrapBreakerInvoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        URL url = invoker.getUrl();
        if(checkNeedCircuitBreak(invoker, invocation)){
            if(logger.isDebugEnabled()){
                logger.debug("activate the circuit break for url ["+invoker.getUrl()+"],invoke method ["+invocation.getMethodName()+"]");
            }
            return doCircuitBreak(invoker, invocation);
        }
        try{
            Result result = invoker.invoke(invocation);
            toBeNormal(invoker, invocation);
            return result;
        }catch (RpcException e){
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
        StringBuffer methodConfig = new StringBuffer(DUBBO_REFERENCE_PREFIX);
        methodConfig.append(interfaceName).append(".").append(method);
        String methodKey = methodConfig.toString();
        //从其中删除对应的异常计数器
        BreakCounter counter = breakCounterMap.remove(methodKey);
        if(counter!=null){
            //将这个counter设置为失效
            counter.disable();
        }
    }

    private boolean checkNeedCircuitBreak(Invoker<?> invoker, Invocation invocation) {
        String interfaceName = invoker.getUrl().getParameter(Constants.INTERFACE_KEY);
        String method = invocation.getMethodName();
        StringBuffer interfaceConfig = new StringBuffer(DUBBO_REFERENCE_PREFIX);
        interfaceConfig.append(interfaceName);
        StringBuffer methodConfig = new StringBuffer(interfaceConfig.toString());
        methodConfig.append(".").append(method);
        String methodKey = methodConfig.toString();
        int limit = getBreakLimit(interfaceConfig,methodConfig);
        BreakCounter breakCounter = breakCounterMap.get(methodKey);
        if(breakCounter!=null&&breakCounter.enable()){
            long currentBreakCount = breakCounter.getCurrentBreakCount();
            int retries=0;
            retries=invoker.getUrl().getParameter(Constants.RETRIES_KEY, Constants.DEFAULT_RETRIES);
            retries++;
            limit=limit*retries;
            if(logger.isDebugEnabled()){
                logger.debug("check invoke "+interfaceName+"."+method+"() circuit break,current break count ["+currentBreakCount+"],the limit is ["+limit+"]");
            }
            if(limit<=currentBreakCount){
                if(needRetry(invoker, invocation, currentBreakCount)){
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
        StringBuffer interfaceConfig = new StringBuffer(DUBBO_REFERENCE_PREFIX);
        interfaceConfig.append(interfaceName);
        StringBuffer methodConfig = new StringBuffer(interfaceConfig.toString());
        methodConfig.append(".").append(method);
        int frequency = getRetryFrequency(interfaceConfig, methodConfig);
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
        CricuitBreakerException baseBusinessException = new CricuitBreakerException(DEFAULT_CIRCUIT_BREAKER_ERROR_CODE,"哎哟，系统异常，请稍后再试");
        RpcException rpcException = new RpcException(RpcException.BIZ_EXCEPTION,baseBusinessException);
        throw rpcException;
    }

    private void incrementBreakCount(Invoker<?> invoker,Invocation invocation){
        String interfaceName = invoker.getUrl().getParameter(Constants.INTERFACE_KEY);
        String method = invocation.getMethodName();
        StringBuffer interfaceConfig = new StringBuffer(DUBBO_REFERENCE_PREFIX);
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
        StringBuffer interfaceConfig = new StringBuffer(DUBBO_REFERENCE_PREFIX);
        interfaceConfig.append(interfaceName);
        StringBuffer methodConfig = new StringBuffer(interfaceConfig.toString());
        methodConfig.append(".").append(method);
        String methodKey = methodConfig.toString();
        ExceptionMarker breakMarker = new ExceptionMarker(System.currentTimeMillis(), getBreakTimeInterval(interfaceConfig, methodConfig),e);
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


    /**
     * 获取某个方法或者某个接口的判定为出现异常的次数
     * 提供在配置中心和dubbo.properties两种途径配置
     * 如果两个地方均有配置，配置中心的为准
     * @param interfaceConfig
     * @param methodConfig
     * @return
     */
    private int getBreakLimit(StringBuffer interfaceConfig,StringBuffer methodConfig){
        methodConfig.append(".break.limit");
        interfaceConfig.append(".break.limit");
        String breakLimitConf = ConfigUtils.getProperty(methodConfig.toString(), ConfigUtils.getProperty(interfaceConfig.toString(), ConfigUtils.getProperty("dubbo.reference.default-break-limit", DEFAULT_BREAK_LIMIT)));
        int breakLimit =  Integer.parseInt(breakLimitConf);
        return breakLimit;
    }


    /**
     * 获取某个方法或者接口多少时间内出现指定次数异常判定为服务器宕机
     * 提供在配置中心和dubbo.properties两种途径配置
     * 如果两个地方均有配置，配置中心的为准
     * @param interfaceConfig
     * @param methodConfig
     * @return
     */
    private long getBreakTimeInterval(StringBuffer interfaceConfig, StringBuffer methodConfig){
        methodConfig.append(".break.time.interval");
        interfaceConfig.append(".break.time.interval");
        String timeIntervalConf = ConfigUtils.getProperty(methodConfig.toString(), ConfigUtils.getProperty(interfaceConfig.toString(), ConfigUtils.getProperty("dubbo.reference.default-break-time-interval", DEFAULT_TIME_INTERVAL)));
        long timeInterval =  Long.parseLong(timeIntervalConf);
        return timeInterval;
    }

    /**
     * 获取某个方法或者接口重试频次，及每出现多少次异常，就重试一次远程接口
     * 提供在配置中心和dubbo.properties两种途径配置
     * 如果两个地方均有配置，配置中心的为准
     * @param interfaceConfig
     * @param methodConfig
     * @return
     */
    private int getRetryFrequency(StringBuffer interfaceConfig, StringBuffer methodConfig){
        methodConfig.append(".retry.frequency");
        interfaceConfig.append(".retry.frequency");
        String retryFrequencyConf = ConfigUtils.getProperty(methodConfig.toString(), ConfigUtils.getProperty(interfaceConfig.toString(), ConfigUtils.getProperty("dubbo.reference.default_retry_frequency", DEFAULT_RETRY_FREQUENCY)));
        int retryFrequency = Integer.parseInt(retryFrequencyConf);
        return retryFrequency;
    }

    /**
     * 获取这个功能是否开启的开关
     * @return
     */
    private boolean getFunctionSwitch(){
        String configSwitch = ConfigUtils.getProperty(DUBBO_REFERENCE_CIRCUIT_BREAKER_SWITCH,"true");
        return Boolean.parseBoolean(configSwitch);
    }
}
