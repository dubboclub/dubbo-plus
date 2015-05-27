package com.dubboclub.cache.memcache;

import com.alibaba.dubbo.cache.Cache;
import com.alibaba.dubbo.common.URL;
import com.dubboclub.cache.AbstractCache;
import com.dubboclub.cache.RemoteCache;
import com.dubboclub.cache.remote.MemcachedClient;
import com.dubboclub.cache.remote.RemoteClient;

import java.rmi.Remote;

/**
 * Created by bieber on 2015/5/26.
 */
public class MCCache extends RemoteCache {

    public MCCache(String cacheName, URL url) {
        super(cacheName, url, new MemcachedClient());
    }
    
}
