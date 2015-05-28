package com.dubboclub.cache.config;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedisPool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
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
        InputStream inputStream;
        if(propertyFile.startsWith(CLASS_PATH_PREFIX)){
            inputStream =  CacheConfig.class.getClassLoader().getResourceAsStream(propertyFile.replace(CLASS_PATH_PREFIX, ""));
        }else if(propertyFile.startsWith(FILE_PATH_PREFIX)){
            inputStream = new FileInputStream(new File(propertyFile.replace(FILE_PATH_PREFIX,"")));
        }else{
            inputStream =  CacheConfig.class.getClassLoader().getResourceAsStream(propertyFile);
        }
        Properties properties = new Properties();
        properties.load(inputStream);
        return properties;
    }
}
