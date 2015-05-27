package com.dubboclub.cache.mixcache;

import com.alibaba.dubbo.cache.Cache;
import com.alibaba.dubbo.common.URL;
import com.dubboclub.cache.AbstractCache;
import com.dubboclub.cache.AbstractCacheFactory;

/**
 * Created by bieber on 2015/5/27.
 */
public class MixCacheFactory extends AbstractCacheFactory {


    @Override
    protected Cache generateNewCache(String cacheName, URL url) {
        return new MixCache(cacheName,url);
    }
}
