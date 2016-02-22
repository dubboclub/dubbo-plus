package net.dubboclub.restful.export.mapping;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;

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

    public ServiceHandler(String group, String version, Class<T> serviceType, T impl) {
        if(serviceType==null){
            throw new IllegalArgumentException("[serviceType] must not be null");
        }
        this.group = group;
        this.version = version;
        this.serviceType = serviceType;
        this.impl = impl;
        methodHandlerList =new ArrayList<MethodHandler>();
        initHandler();
    }


    private void initHandler(){
        Class type = this.serviceType;
        while(type!=Object.class){
            Method[] methods = type.getDeclaredMethods();
            for(Method method:methods){
                MethodHandler methodHandler = new MethodHandler(method.getName(),method,method.getParameterTypes(),this.impl);
                methodHandlerList.add(methodHandler);
            }
            type = type.getSuperclass();
        }
    }


    public MethodHandler mapping(RequestEntity requestEntity){
        for(MethodHandler methodHandler:methodHandlerList){
            if(methodHandler.support(requestEntity.getMethod())){
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
