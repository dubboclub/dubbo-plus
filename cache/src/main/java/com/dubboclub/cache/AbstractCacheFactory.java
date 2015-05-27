package com.dubboclub.cache;

import com.alibaba.dubbo.cache.Cache;
import com.alibaba.dubbo.cache.CacheFactory;
import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.dubboclub.cache.redis.RedisCache;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by bieber on 2015/5/27.
 */
public abstract class AbstractCacheFactory implements CacheFactory{
    
    private ConcurrentHashMap<String,Cache> CACHE_MAP =new ConcurrentHashMap<String, Cache>();

    protected String generateCacheName(URL url){
        return url.getParameter(Constants.INTERFACE_KEY)+"."+url.getParameter(Constants.METHOD_KEY);
    }

    @Override
    public Cache getCache(URL url) {
        String cacheName = generateCacheName(url);
        Cache cache = generateNewCache(cacheName,url);
        cache=putCacheIfAbsent(cacheName,cache);
        return cache;
    }
    
    protected abstract Cache generateNewCache(String cacheName,URL url);

    protected Cache putCacheIfAbsent(String cacheName,Cache cache){
        if(CACHE_MAP.containsKey(cacheName)){
            return CACHE_MAP.get(cacheName);
        }
        Cache oldCache = CACHE_MAP.putIfAbsent(cacheName, cache);
        if(oldCache==null){
            return cache;
        }
        return oldCache;
    }
}


