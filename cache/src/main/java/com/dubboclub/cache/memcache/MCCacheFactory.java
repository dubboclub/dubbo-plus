package com.dubboclub.cache.memcache;

import com.alibaba.dubbo.cache.Cache;
import com.alibaba.dubbo.cache.CacheFactory;
import com.alibaba.dubbo.common.URL;

/**
 * Created by bieber on 2015/5/26.
 */
public class MCCacheFactory implements CacheFactory {
    @Override
    public Cache getCache(URL url) {
        return null;
    }
}
