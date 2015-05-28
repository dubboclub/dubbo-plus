package com.dubboclub.cache.remote;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.dubboclub.cache.config.CacheConfig;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by bieber on 2015/5/27.
 */
public abstract class RemoteClient {
    
    protected static final Logger logger = LoggerFactory.getLogger(RemoteClient.class);
    
    private static final String[] SUFFIXS={"Client"};
    
    public abstract void cacheValue(byte[] key,byte[] bytes,int expireSecond);
    
    public abstract byte[] getValue(byte[] key);
    
    
    protected static void appendProperties(Object object,Class<?> owner){
        Field[] fields = object.getClass().getDeclaredFields();
        String prefix="cache."+getTagName(owner)+".";
        for(Field field:fields){
            Method setMethod = getSetMethod(object.getClass(),field);
            if(setMethod!=null&&setMethod.isAccessible()&&field.getType().isPrimitive()){
                String property = StringUtils.camelToSplitName(field.getName(), ".");
                String configValue=null;
                if(CacheConfig.getProperties().contains(prefix+property)){
                    try {
                        configValue=CacheConfig.getProperty(prefix+property);
                        setMethod.invoke(object,casePrimitiveType(field.getType(),configValue));
                    } catch (IllegalAccessException e) {
                        logger.debug("Failed to set value ["+configValue+"] property ["+field.getName()+"] ",e);
                    } catch (InvocationTargetException e) {
                        logger.debug("Failed to set value [" + configValue + "] property [" + field.getName() + "] ",e);
                    }
                }
            }
        }
    }

    private static String getTagName(Class<?> cls) {
        String tag = cls.getSimpleName();
        for (String suffix : SUFFIXS) {
            if (tag.endsWith(suffix)) {
                tag = tag.substring(0, tag.length() - suffix.length());
                break;
            }
        }
        tag = tag.toLowerCase();
        return tag;
    }

    protected static Object casePrimitiveType(Class<?> targetType, Object value) {
        if (value == null) {
            return null;
        }
        if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value.toString().trim());
        } else if (targetType == short.class || targetType == Short.class) {
            return Short.parseShort(value.toString().trim());
        } else if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(value.toString().trim());
        } else if (targetType == float.class || targetType == Float.class) {
            return Float.parseFloat(value.toString().trim());
        } else if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(value.toString().trim());
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value.toString().trim());
        } else if (targetType == char.class || targetType == Character.class) {
            return value.toString().charAt(0);
        } else if (targetType.isEnum()) {
            Class<? extends Enum> enumClass = (Class<? extends Enum>) targetType;
            return Enum.valueOf(enumClass, value.toString());
        } else {
            return value;
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
