package net.dubboclub.cache;

import net.dubboclub.cache.config.CacheConfig;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.Configuration;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by bieber on 2015/5/28.
 */
public class GenerateEhCacheDefaultConfig {
    
    public static void main(String[] args) throws IllegalAccessException {
        CacheManager manager = CacheManager.create();
        Configuration configuration = manager.getConfiguration();
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        Object object= poolConfig;
        Field[] fields = object.getClass().getDeclaredFields();
        String prefix="cache.ehcache.";
        for(Field field:fields){
           field.setAccessible(true);
            Method method = getSetMethod(object.getClass(),field);
            if(method!=null&& CacheConfig.checkIsBasicType(field.getType())){
                System.out.println("#默认值"+field.get(object));
                System.out.println(prefix+field.getName());
            }
        }
    }
    protected static Method getSetMethod(Class<?> clazz,Field field)   {
        StringBuffer methodName = new StringBuffer("set");
        String fieldName = field.getName();
        methodName.append(fieldName.substring(0,1).toUpperCase()).append(fieldName.substring(1));
            Method[] methods = clazz.getDeclaredMethods();
            for(Method method:methods){
                if(method.getName().equals(methodName.toString())){
                    return method;
                }
            }

        return null;
    }
}
