package com.dubboclub.cache.redis;

import com.alibaba.dubbo.cache.Cache;
import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.serialize.Serialization;
import com.dubboclub.cache.AbstractCache;
import com.dubboclub.cache.remote.RedisClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by bieber on 2015/5/26.
 */
public class RedisCache extends AbstractCache {
    
    private int expireSecond;
    
    protected  RedisCache(String cachedTarget,URL url){
        this.cachedTarget=objectToBytes(url,cachedTarget);
        cachedUrl=url;
        expireSecond=getExpireSecond(url);
    }
    
    @Override
    public void put(Object key, Object value) {
        byte[] bytes = generateCacheKey(key);
        if(bytes!=null){
            RedisClient.cacheValue(generateCacheKey(key),objectToBytes(cachedUrl,value),expireSecond);
        }
    }

    @Override
    public Object get(Object key) {
        byte[] bytes = generateCacheKey(key);
        if(bytes!=null){
             return bytesToObject(cachedUrl,RedisClient.getValue(bytes));
        }
        return null;
    }
}
