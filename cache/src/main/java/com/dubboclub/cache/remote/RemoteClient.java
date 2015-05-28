package com.dubboclub.cache.remote;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.dubboclub.cache.config.CacheConfig;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by bieber on 2015/5/27.
 */
public abstract class RemoteClient {
    
    protected static final Logger logger = LoggerFactory.getLogger(RemoteClient.class);
    
    public abstract void cacheValue(byte[] key,byte[] bytes,int expireSecond);
    
    public abstract byte[] getValue(byte[] key);
    

}
