package com.dubboclub.cache.redis;

import com.alibaba.dubbo.cache.Cache;
import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.serialize.Serialization;
import com.dubboclub.cache.AbstractCache;
import com.dubboclub.cache.RemoteCache;
import com.dubboclub.cache.remote.RedisClient;
import com.dubboclub.cache.remote.RemoteClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by bieber on 2015/5/26.
 */
public class RedisCache extends RemoteCache {
    
    private RemoteClient remoteClient;

    @Override
    protected String getTagName() {
        return "redis";
    }

    protected  RedisCache(String cachedTarget,URL url){
        super(cachedTarget,url,new RedisClient());
    }
    
}
