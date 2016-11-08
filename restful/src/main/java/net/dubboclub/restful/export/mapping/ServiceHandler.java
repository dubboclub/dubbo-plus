package net.dubboclub.restful.export.mapping;


import com.alibaba.dubbo.common.Constants;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @date: 2016/2/22.
 * @author:bieber.
 * @project:dubbo-plus.
 * @package:net.dubboclub.restful.export.container.
 * @version:1.0.0
 * @fix:
 * @description: 描述功能
 */
public class ServiceHandler<T extends Object> {

    private String group;

    private String version;

    private Class<T> serviceType;

    private T impl;

    private List<MethodHandler> methodHandlerList;

    private String path;

    public ServiceHandler(String group, String version, Class<T> serviceType,String path, T impl) {
        if(serviceType==null){
            throw new IllegalArgumentException("[serviceType] must not be null");
        }
        this.group = Constants.ANY_VALUE.equals(group)?null:group;
        this.version = Constants.ANY_VALUE.equals(version)?null:version;
        this.serviceType = serviceType;
        this.impl = impl;
        methodHandlerList =new ArrayList<MethodHandler>();
        this.path=path;
        initHandler();
    }


    public String getPath() {
        return path;
    }

    private void initHandler(){
        Class type = this.serviceType;
        while(type!=null&&type!=Object.class){
            Method[] methods = type.getDeclaredMethods();
            for(Method method:methods){
                MethodHandler methodHandler = new MethodHandler(method.getName(),method,method.getParameterTypes(),this.impl);
                methodHandlerList.add(methodHandler);
            }
            type = type.getSuperclass();
        }
    }


    public List<MethodHandler> getMethodHandlerList() {
        return methodHandlerList;
    }

    public MethodHandler mapping(RequestEntity requestEntity){
        for(MethodHandler methodHandler:methodHandlerList){
            if(methodHandler.support(requestEntity)){
                return methodHandler;
            }
        }
        return null;
    }


    public String getGroup() {
        return group;
    }

    public String getVersion() {
        return version;
    }

    public Class<T> getServiceType() {
        return serviceType;
    }

    public T getImpl() {
        return impl;
    }

    @Override
    public String toString() {
        return "ServiceHandler{" +
                "serviceType=" + serviceType.getName() +
                ", version='" + version + '\'' +
                ", group='" + group + '\'' +
                '}';
    }
}
