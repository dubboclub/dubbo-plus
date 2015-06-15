package net.dubboclub.cache.memcache;

import com.alibaba.dubbo.common.URL;
import net.dubboclub.cache.RemoteCache;
import net.dubboclub.cache.remote.MemcachedClient;

/**
 * Created by bieber on 2015/5/26.
 */
public class MemcachedCache extends RemoteCache {

    @Override
    protected String getTagName() {
        return "memcached";
    }

    public MemcachedCache(String cacheName, URL url) {
        super(cacheName, url, new MemcachedClient());
    }
    
}
