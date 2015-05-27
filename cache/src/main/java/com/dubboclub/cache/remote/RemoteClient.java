package com.dubboclub.cache.remote;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by bieber on 2015/5/27.
 */
public abstract class RemoteClient {

    public abstract void cacheValue(byte[] key,byte[] bytes,int expireSecond);
    
    public abstract byte[] getValue(byte[] key);

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
