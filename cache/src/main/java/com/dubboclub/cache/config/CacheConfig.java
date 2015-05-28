package com.dubboclub.cache.config;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.common.utils.StringUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Properties;

/**
 * Created by bieber on 2015/5/27.
 */
public class CacheConfig {
    private static final String CACHE_PROPERTIES_FILE = "cache.properties.file";

    private static final String CLASS_PATH_PREFIX="classpath:";
    
    private static final String FILE_PATH_PREFIX="file:";
    
    private static final Logger logger = LoggerFactory.getLogger(CacheConfig.class);
    
    private static volatile Properties properties;

    private static final String[] SUFFIXS={"Client"};
    
    static {
        initProperties();
    }
    public static void initProperties(){
        if(properties==null){
            synchronized (CacheConfig.class){
                if(properties==null){
                    String cacheConfig = ConfigUtils.getProperty(CACHE_PROPERTIES_FILE, System.getProperty(CACHE_PROPERTIES_FILE));
                    try {
                        properties =  ConfigUtils.getProperties();
                        if(!StringUtils.isEmpty(cacheConfig)){
                            properties=loadProperties(cacheConfig);
                        }
                    } catch (IOException e) {
                        logger.error("failed to open redis config file",e);
                    }
                }
            }
        }
        
    }
    
    public static Properties getProperties(){
        if(properties==null){
            initProperties();
        }
        return properties;
    }
    
    public static String getProperty(String key){
        Object value = properties.get(key);
        if(value==null){
            return null;
        }else{
            return value.toString();
        }
    }
    
    public static String getProperty(String key,String defaultValue){
        String value = getProperty(key);
        if(!StringUtils.isEmpty(value)){
            return value;
        }
        return defaultValue;
    }
    
    public static int getProperty(String key,int defaultValue){
        String value = getProperty(key);
        if(!StringUtils.isEmpty(value)){
            return Integer.parseInt(value);
        }
        return defaultValue;
    }
    public static short getProperty(String key,short defaultValue){
        String value = getProperty(key);
        if(!StringUtils.isEmpty(value)){
            return Short.parseShort(value);
        }
        return defaultValue;
    }
    public static long getProperty(String key,long defaultValue){
        String value = getProperty(key);
        if(!StringUtils.isEmpty(value)){
            return Long.parseLong(value);
        }
        return defaultValue;
    }
    public static float getProperty(String key,float defaultValue){
        String value = getProperty(key);
        if(!StringUtils.isEmpty(value)){
            return Float.parseFloat(value);
        }
        return defaultValue;
    }
    public static double getProperty(String key,double defaultValue){
        String value = getProperty(key);
        if(!StringUtils.isEmpty(value)){
            return Double.parseDouble(value);
        }
        return defaultValue;
    }
    
    public static boolean getProperty(String key ,boolean defaultValue){
        String value = getProperty(key);
        if(!StringUtils.isEmpty(value)){
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }
    private static Properties loadProperties(String propertyFile) throws IOException {
        InputStream inputStream = getConfigurationInputStream(propertyFile);
        Properties properties = new Properties();
        properties.load(inputStream);
        return properties;
    }
    
    public static InputStream getConfigurationInputStream(String propertyFile) throws FileNotFoundException {
        InputStream inputStream;
        if(propertyFile.startsWith(CLASS_PATH_PREFIX)){
            inputStream =  CacheConfig.class.getClassLoader().getResourceAsStream(propertyFile.replace(CLASS_PATH_PREFIX, ""));
        }else if(propertyFile.startsWith(FILE_PATH_PREFIX)){
            inputStream = new FileInputStream(new File(propertyFile.replace(FILE_PATH_PREFIX,"")));
        }else{
            inputStream =  CacheConfig.class.getClassLoader().getResourceAsStream(propertyFile);
        }
        return inputStream;
    }

    public static void appendProperties(Object object,Class<?> owner){
        Field[] fields = object.getClass().getDeclaredFields();
        String prefix="cache."+getTagName(owner)+".";
        for(Field field:fields){
            Method setMethod = getSetMethod(object.getClass(),field);
            if(setMethod!=null&&checkIsBasicType(field.getType())){
                String property = StringUtils.camelToSplitName(field.getName(), ".");
                String configValue=null;
                if(CacheConfig.getProperties().containsKey(prefix+property)){
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

    private static Method getSetMethod(Class<?> clazz,Field field)   {
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

    public static boolean checkIsBasicType(Class<?> targetType) {
        return targetType.isEnum() || targetType.isPrimitive()
                || String.class == targetType || targetType == Integer.class
                || targetType == Short.class || targetType == Long.class
                || targetType == Boolean.class || targetType == Double.class
                || targetType == Float.class || targetType == File.class
                || targetType == Character.class ;
    }
}
