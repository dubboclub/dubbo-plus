package com.dubboclub.cache;

import com.alibaba.dubbo.common.URL;
import com.dubboclub.cache.remote.RemoteClient;

/**
 * Created by bieber on 2015/5/27.
 */
public abstract class RemoteCache extends AbstractCache {
    
    protected RemoteClient remoteClient;
    
    public RemoteCache(String cacheName, URL url,RemoteClient remoteClient) {
        super(cacheName, url);
        this.remoteClient = remoteClient;
    }

    @Override
    public void put(Object key, Object value) {
        byte[] bytes = generateCacheKey(key);
        if(bytes!=null){
            remoteClient.cacheValue(generateCacheKey(key),objectToBytes(cachedUrl,value),expireSecond);
        }
    }

    @Override
    public Object get(Object key) {
        byte[] bytes = generateCacheKey(key);
        if(bytes!=null){
            return bytesToObject(cachedUrl,remoteClient.getValue(bytes));
        }
        return null;
    }
}
