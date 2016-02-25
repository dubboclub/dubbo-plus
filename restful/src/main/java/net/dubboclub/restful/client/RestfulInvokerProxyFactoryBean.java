package net.dubboclub.restful.client;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.fastjson.JSON;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @date: 2016/2/26.
 * @author:bieber.
 * @project:dubbo-plus.
 * @package:net.dubboclub.restful.client.
 * @version:1.0.0
 * @fix:
 * @description: 描述功能
 */
public class RestfulInvokerProxyFactoryBean implements MethodInterceptor {

    private Object serviceProxy;

    private Class<?> serviceType;

    private String baseUrl;

    private String versionGroupFragment="";

    public RestfulInvokerProxyFactoryBean(URL url,Class<?> serviceType) {
        this.serviceProxy = new ProxyFactory(serviceType, this).getProxy();
        StringBuffer reqUrl = new StringBuffer("");
        baseUrl=url.toIdentityString();
        if(url.hasParameter(Constants.VERSION_KEY)){
            reqUrl.append("/").append(url.getParameter(Constants.VERSION_KEY));
        }else{
            reqUrl.append("/").append("all");
        }
        if(url.hasParameter(Constants.GROUP_KEY)){
            reqUrl.append("/").append(url.getParameter(Constants.GROUP_KEY));
        }else{
            reqUrl.append("/").append("all");
        }
        versionGroupFragment = reqUrl.toString();
        this.serviceType = serviceType;
    }

    public Object getProxy(){
        return serviceProxy;
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        Object[] args = methodInvocation.getArguments();
        Map<String,String> requestMap = new HashMap<String, String>();
        if(args!=null){
            for(int i=0;i<args.length;i++){
                requestMap.put("arg"+(i+1), JSON.toJSONString(args[i]));
            }
        }
        try {
            byte[] response = HttpInvoker.post(baseUrl+"/"+methodInvocation.getMethod().getName()+versionGroupFragment,
                    JSON.toJSONBytes(requestMap), RpcContext.getContext().getAttachments());
            Class<?> retType = methodInvocation.getMethod().getReturnType();
            if(retType!=Void.class&&retType!=Void.TYPE){
                return JSON.parseObject(response,retType);
            }else{
                return null;
            }
        } catch (IOException e) {
            throw new RpcException(RpcException.NETWORK_EXCEPTION,"fail to invoke restful remote service",e);
        }
    }
}
