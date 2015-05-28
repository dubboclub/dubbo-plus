package com.dubboclub.cache;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.dubboclub.cache.config.CacheConfig;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.Configuration;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by bieber on 2015/5/28.
 */
public class GenerateEhCacheDefaultConfig {
    
    public static void main(String[] args) throws IllegalAccessException {
        CacheManager manager = CacheManager.create();
        Configuration configuration = manager.getConfiguration();
      
        Object object=configuration.getDefaultCacheConfiguration();
        Field[] fields = object.getClass().getDeclaredFields();
        String prefix="cache.ehcache.";
        for(Field field:fields){
           field.setAccessible(true);
            Method method = getSetMethod(object.getClass(),field);
            if(method!=null&& CacheConfig.checkIsBasicType(field.getType())){
                System.out.println(prefix+StringUtils.camelToSplitName(field.getName(),".")+"#默认值"+field.get(object));
            }
        }
    }
    protected static Method getSetMethod(Class<?> clazz,Field field)   {
        StringBuffer methodName = new StringBuffer("set");
        String fieldName = field.getName();
        methodName.append(fieldName.substring(0,1).toUpperCase()).append(fieldName.substring(1));
        try {
            return clazz.getMethod(methodName.toString(),field.getType());
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
