package com.dubboclub.cache.remote;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.dubboclub.cache.config.CacheConfig;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
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
import java.util.concurrent.TimeUnit;

/**
 * Created by bieber on 2015/5/26.
 */
public class RedisClient {
    private static final Logger logger = LoggerFactory.getLogger(RedisClient.class);
    private static final String REDIS_CONNECT="cache.redis.connect";
    private static  ShardedJedisPool JEDIS_POOL;
    static {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        try {
            Field[] fields = poolConfig.getClass().getDeclaredFields();
            for(Field field:fields){
                Method setMethod = getSetMethod(poolConfig.getClass(),field);
                if(CacheConfig.getProperties().contains(field.getName())&&setMethod!=null&&setMethod.isAccessible()){
                    setMethod.invoke(poolConfig,casePrimitiveType(field.getType(), CacheConfig.getProperty(field.getName())));
                }
            }
            
            Object redisConnect = CacheConfig.getProperty(REDIS_CONNECT);
            if(redisConnect==null|| StringUtils.isEmpty(redisConnect.toString())){
                throw new IllegalArgumentException("cache.redis.connect must not empty");
            }
            
            String connectString = redisConnect.toString();
            String[] connects = connectString.split(",");
            List<JedisShardInfo> jedisShardInfoList = new ArrayList<JedisShardInfo>();
            for(String connect:connects){
                String[] splits = connect.split(":");
                JedisShardInfo jedisShardInfo = new JedisShardInfo(splits[0],splits[1]);
                jedisShardInfoList.add(jedisShardInfo);
            }
            JEDIS_POOL=new ShardedJedisPool(poolConfig,jedisShardInfoList);
        } catch (IllegalAccessException e) {
            logger.error("failed to config redis pool",e);
        } catch (InvocationTargetException e) {
            logger.error("failed to config redis pool", e);
        }
    }
    
    public static void cacheValue(byte[] key,byte[] bytes,int expireSecond){
        ShardedJedis jedis = JEDIS_POOL.getResource();
        try{
            if(expireSecond>0){
                jedis.setex(key,expireSecond,bytes);
            }else{
                jedis.set(key,bytes);
            }
        }finally {
            JEDIS_POOL.returnResource(jedis);
        }
    }
    
    public static byte[] getValue(byte[] key){
        ShardedJedis jedis = JEDIS_POOL.getResource();
        try{
            return jedis.get(key);
        }finally {
            JEDIS_POOL.returnResource(jedis);
        }
    }
    
    private static Object casePrimitiveType(Class<?> targetType, Object value) {
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
        try {
            return clazz.getMethod(methodName.toString(),field.getType());
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
