package net.dubboclub.cache.mixcache;

import com.alibaba.dubbo.cache.Cache;
import com.alibaba.dubbo.common.URL;
import net.dubboclub.cache.AbstractCache;

/**
 * Created by bieber on 2015/5/27.
 */
public class MixCache extends AbstractCache {

    private Cache l1Cache;
    
    private Cache l2Cache;
    
    public MixCache(Cache l1Cache, Cache l2Cache,String cacheName, URL url){
        this(cacheName,url);
        this.l1Cache=l1Cache;
        this.l2Cache=l2Cache;
    }

    @Override
    protected String getTagName() {
        return "mixcache";
    }

    public MixCache(String cacheName, URL url) {
        super(cacheName, url);
    }

    @Override
    public void put(Object key, Object value) {
        if(key==null||value==null){
            return ;
        }
        l1Cache.put(key,value);
        l2Cache.put(key,value);
    }

    @Override
    public Object get(Object key) {
        if(key==null){
            return null;
        }
        Object value=l1Cache.get(key);
        if(value==null){
            value=l2Cache.get(key);
        }
        return value;
    }
}
