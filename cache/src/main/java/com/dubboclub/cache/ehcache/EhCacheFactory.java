package com.dubboclub.cache.ehcache;

import com.alibaba.dubbo.cache.Cache;
import com.alibaba.dubbo.common.URL;
import com.dubboclub.cache.AbstractCacheFactory;
import com.dubboclub.cache.redis.RedisCache;

/**
 * Created by bieber on 2015/5/27.
 */
public class EhCacheFactory extends AbstractCacheFactory {

    @Override
    protected Cache generateNewCache(String cacheName, URL url) {
        return new ECCache(cacheName,url);
    }
}
