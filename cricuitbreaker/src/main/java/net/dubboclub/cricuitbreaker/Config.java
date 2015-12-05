package net.dubboclub.cricuitbreaker;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import org.apache.commons.lang.StringUtils;

import java.net.InetAddress;

/**
 * Created by bieber on 2015/6/15.
 */
public class Config {

    //默认情况下1分钟内出现10个异常，同时默认情况下当满足1分钟内10个异常，然后每发生10次错误就会进行一次重试，检查服务是否恢复
    private static final String DEFAULT_BREAK_LIMIT="10";

    private static final String DEFAULT_RETRY_FREQUENCY="10";

    protected static final String DUBBO_REFERENCE_PREFIX="dubbo.reference.";

    private static final String DUBBO_REFERENCE_CIRCUIT_BREAKER_SWITCH=DUBBO_REFERENCE_PREFIX+"circuit.breaker";

    /**
     * 获取当前调用的服务方法是否开启了服务降级功能
     *  @param invoker
     * @param invocation
     * @return
     */
    public static boolean checkFunctionSwitch(Invoker<?> invoker, Invocation invocation){
        //先动态从调用地址里面获取开关配置信息，这个配置可以在配置中心的动态配置{methodName.}circuit.bread=true/false
        String circuitBreak = invoker.getUrl().getMethodParameter(invocation.getMethodName(),"circuit.break");
        if(StringUtils.isEmpty(circuitBreak)){
            String interfaceName = invoker.getUrl().getParameter(Constants.INTERFACE_KEY);
            String method = invocation.getMethodName();
            StringBuffer interfaceConfig = new StringBuffer(DUBBO_REFERENCE_PREFIX);
            interfaceConfig.append(interfaceName);
            StringBuffer methodConfig = new StringBuffer(interfaceConfig.toString());
            methodConfig.append(".").append(method);
            return getSwitch(interfaceConfig,methodConfig);
        }
        return Boolean.parseBoolean(StringUtils.trim(circuitBreak));
    }
    
    public static int getBreakLimit(Invoker<?> invoker,Invocation invocation){
        String dynamicConfig = invoker.getUrl().getMethodParameter(invocation.getMethodName(),"break.limit");
        if(StringUtils.isEmpty(dynamicConfig)){
            return getBreakLimit(getInterfacePropertyName(invoker),getMethodPropertyName(invoker,invocation));
        }
        int limit =  Integer.parseInt(dynamicConfig);
        int retries = invoker.getUrl().getParameter(Constants.RETRIES_KEY, Constants.DEFAULT_RETRIES);
        retries++;
        return limit*retries;
    }
    public static StringBuffer getInterfacePropertyName(Invoker<?> invoker){
        String interfaceName = invoker.getUrl().getParameter(Constants.INTERFACE_KEY);
        StringBuffer interfaceConfig = new StringBuffer(DUBBO_REFERENCE_PREFIX);
        interfaceConfig.append(interfaceName);
        return interfaceConfig;
    }

    public static StringBuffer getMethodPropertyName(Invoker<?> invoker, Invocation invocation){
        String interfaceName = invoker.getUrl().getParameter(Constants.INTERFACE_KEY);
        String method = invocation.getMethodName();
        StringBuffer interfaceConfig = new StringBuffer(DUBBO_REFERENCE_PREFIX);
        interfaceConfig.append(interfaceName);
        StringBuffer methodConfig = new StringBuffer(interfaceConfig.toString());
        methodConfig.append(".").append(method);
        return methodConfig;
    }
    
    public static InetAddress getLocalAddress(){
        return NetUtils.getLocalAddress();
    }
    /**
     * 获取某个方法或者某个接口的判定为出现异常的次数
     * 提供在配置中心和dubbo.properties两种途径配置
     * 如果两个地方均有配置，配置中心的为准
     * @param interfaceConfig
     * @param methodConfig
     * @return
     */
    public static int getBreakLimit(StringBuffer interfaceConfig,StringBuffer methodConfig){
        methodConfig.append(".break.limit");
        interfaceConfig.append(".break.limit");
        String breakLimitConf = ConfigUtils.getProperty(methodConfig.toString(), ConfigUtils.getProperty(interfaceConfig.toString(), ConfigUtils.getProperty("dubbo.reference.default.break.limit", DEFAULT_BREAK_LIMIT)));
        return Integer.parseInt(breakLimitConf);
    }

    /**
     * 获取某个方法或者接口是否开启服务降级的功能，默认是开启降级功能
     * 提供在配置中心和dubbo.properties两种途径配置
     * 如果两个地方均有配置，配置中心的为准
     * @param interfaceConfig
     * @param methodConfig
     * @return
     */
    public static boolean getSwitch(StringBuffer interfaceConfig, StringBuffer methodConfig){
        methodConfig.append(".circuit.break");
        interfaceConfig.append(".circuit.break");
        //从dubbo.properties的配置中获取
        String switchConfig = ConfigUtils.getProperty(methodConfig.toString(), ConfigUtils.getProperty(interfaceConfig.toString(), ConfigUtils.getProperty("dubbo.reference.circuit.break", "false")));
        return Boolean.parseBoolean(switchConfig);
    }

    public static int getRetryFrequency(Invoker<?> invoker,Invocation invocation){
        String retryFrequency = invoker.getUrl().getMethodParameter(invocation.getMethodName(),"retry.frequency");
        if(StringUtils.isEmpty(retryFrequency)){
            return getRetryFrequency(getInterfacePropertyName(invoker),getMethodPropertyName(invoker,invocation));
        }
        return Integer.parseInt(retryFrequency);
    }

    /**
     * 获取某个方法或者接口重试频次，及每出现多少次异常，就重试一次远程接口
     * 提供在配置中心和dubbo.properties两种途径配置
     * 如果两个地方均有配置，配置中心的为准
     * @param interfaceConfig
     * @param methodConfig
     * @return
     */
    public static int getRetryFrequency(StringBuffer interfaceConfig, StringBuffer methodConfig){
        methodConfig.append(".retry.frequency");
        interfaceConfig.append(".retry.frequency");
        String retryFrequencyConf = ConfigUtils.getProperty(methodConfig.toString(), ConfigUtils.getProperty(interfaceConfig.toString(), ConfigUtils.getProperty("dubbo.reference.default.retry.frequency", DEFAULT_RETRY_FREQUENCY)));
        return Integer.parseInt(retryFrequencyConf);
    }
}

