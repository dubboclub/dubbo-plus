package net.dubboclub.restful.export.mapping;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @date: 2016/2/22.
 * @author:bieber.
 * @project:dubbo-plus.
 * @package:net.dubboclub.restful.export.mapping.
 * @version:1.0.0
 * @fix:
 * @description: 描述功能
 */
public class MethodHandler {

    private String methodName;


    private Method method;

    private Class<?>[] argTypes;

    private Object target;

    public MethodHandler(String methodName, Method method, Class<?>[] argTypes, Object target) {
        this.methodName = methodName;
        this.method = method;
        this.argTypes = argTypes;
        this.target = target;
    }

    public Object invoke(Object...args) throws InvocationTargetException, IllegalAccessException {
        return  method.invoke(target,args);
    }

    public String getMethodName() {
        return methodName;
    }

    public Class<?>[] getArgTypes() {
        return argTypes;
    }

    public boolean support(RequestEntity requestEntity){
        if(this.methodName.equals(requestEntity.getMethod())){
            //参数长度比较
            if(this.argTypes.length>0){
                if(requestEntity.getArgs()==null||this.argTypes.length!=requestEntity.getArgs().length){
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "MethodHandler{" +
                "methodName='" + methodName + '\'' +
                ", method=" + method +
                ", argTypes=" + Arrays.toString(argTypes) +
                ", target=" + target.getClass().getName() +
                '}';
    }
}
