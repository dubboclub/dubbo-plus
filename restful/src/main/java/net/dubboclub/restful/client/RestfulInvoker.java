package net.dubboclub.restful.client;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.*;
import com.alibaba.fastjson.JSON;
import net.dubboclub.restful.util.ClassUtils;
import net.dubboclub.restful.util.RestfulConstants;

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
public class RestfulInvoker implements Invoker {

    private Class<?> serviceType;

    private String baseUrl;

    private String versionGroupFragment;

    private URL url;

    private volatile boolean isEnable=true;

    public RestfulInvoker(URL url, Class<?> serviceType) {
        StringBuffer reqUrl = new StringBuffer("");
        baseUrl=url.toIdentityString();
        if(url.hasParameter(Constants.VERSION_KEY)){
            reqUrl.append("/").append(url.getParameter(Constants.VERSION_KEY));
        }else{
            reqUrl.append("/").append(RestfulConstants.ALL);
        }
        if(url.hasParameter(Constants.GROUP_KEY)){
            reqUrl.append("/").append(url.getParameter(Constants.GROUP_KEY));
        }else{
            reqUrl.append("/").append(RestfulConstants.ALL);
        }
        versionGroupFragment = reqUrl.toString();
        this.serviceType = serviceType;
        this.url = url;
    }






    @Override
    public Class getInterface() {
        return serviceType;
    }

    @Override
    public Result invoke(Invocation invocation) throws RpcException {
        Object[] args = invocation.getArguments();
        Map<String,Object> requestMap = new HashMap<String, Object>();
        RpcResult rpcResult = new RpcResult();
        if(args!=null){
            for(int i=0;i<args.length;i++){
                if(args[i]==null){
                    continue;
                }
                requestMap.put("arg"+(i+1), args[i]);
            }
        }
        try{
            byte[] response = HttpInvoker.post(baseUrl+"/"+invocation.getMethodName()+versionGroupFragment,
                    JSON.toJSONBytes(requestMap), RpcContext.getContext().getAttachments());
            Method invokedMethod = serviceType.getMethod(invocation.getMethodName(),invocation.getParameterTypes());
            Class<?> retType = invokedMethod.getReturnType();
            if(retType!=Void.class&&retType!=Void.TYPE){
                rpcResult.setValue(JSON.parseObject(response,retType));
            }
        }catch (Exception e) {
            rpcResult.setException(new RpcException(RpcException.NETWORK_EXCEPTION,"fail to invoke restful remote service",e));
        }
        return rpcResult;
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public boolean isAvailable() {
        return isEnable;
    }

    @Override
    public void destroy() {
        isEnable=false;
    }
}
