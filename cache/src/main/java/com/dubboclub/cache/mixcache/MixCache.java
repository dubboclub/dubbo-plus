package com.dubboclub.cache.mixcache;

import com.alibaba.dubbo.common.URL;
import com.dubboclub.cache.AbstractCache;

/**
 * Created by bieber on 2015/5/27.
 */
public class MixCache extends AbstractCache {
    
    public MixCache(String cacheName, URL url) {
        super(cacheName, url);
    }

    @Override
    public void put(Object o, Object o1) {

    }

    @Override
    public Object get(Object o) {
        return null;
    }
}
