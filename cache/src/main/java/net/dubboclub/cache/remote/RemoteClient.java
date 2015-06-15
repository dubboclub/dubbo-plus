package net.dubboclub.cache.remote;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;

/**
 * Created by bieber on 2015/5/27.
 */
public abstract class RemoteClient {
    
    protected static final Logger logger = LoggerFactory.getLogger(RemoteClient.class);
    
    public abstract void cacheValue(byte[] key,byte[] bytes,int expireSecond);
    
    public abstract byte[] getValue(byte[] key);
    

}
