package net.dubboclub.cache;

import com.alibaba.dubbo.common.URL;
import net.dubboclub.cache.remote.RemoteClient;

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
        if(value==null){
            return ;
        }
        byte[] bytes = generateCacheKey(key);
        if(bytes!=null){
            remoteClient.cacheValue(generateCacheKey(key),objectToBytes(cachedUrl,value),expireSecond);
        }
    }

    @Override
    public Object get(Object key) {
        byte[] bytes = generateCacheKey(key);
        if(bytes!=null){
            byte[] value=remoteClient.getValue(bytes);
            if(value==null){
                return null;
            }
            return bytesToObject(cachedUrl,value);
        }
        return null;
    }
}
