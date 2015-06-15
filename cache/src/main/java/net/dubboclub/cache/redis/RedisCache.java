package net.dubboclub.cache.redis;

import com.alibaba.dubbo.common.URL;
import net.dubboclub.cache.RemoteCache;
import net.dubboclub.cache.remote.RedisClient;

/**
 * Created by bieber on 2015/5/26.
 */
public class RedisCache extends RemoteCache {
    
    @Override
    protected String getTagName() {
        return "redis";
    }

    protected  RedisCache(String cachedTarget,URL url){
        super(cachedTarget,url,new RedisClient());
    }
    
}
