package com.dubboclub.cache.remote;

/**
 * Created by bieber on 2015/5/27.
 */
public class MemcachedClient extends RemoteClient{

    @Override
    public void cacheValue(byte[] key, byte[] bytes, int expireSecond) {

    }

    @Override
    public byte[] getValue(byte[] key) {
        return new byte[0];
    }
}
