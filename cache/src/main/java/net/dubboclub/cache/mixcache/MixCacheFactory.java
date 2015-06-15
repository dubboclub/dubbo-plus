package net.dubboclub.cache.mixcache;

import com.alibaba.dubbo.cache.Cache;
import com.alibaba.dubbo.cache.CacheFactory;
import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.utils.StringUtils;
import net.dubboclub.cache.AbstractCacheFactory;
import net.dubboclub.cache.config.CacheConfig;

/**
 * Created by bieber on 2015/5/27.
 */
public class MixCacheFactory extends AbstractCacheFactory {
    
    private static final String MIX_CACHE="cache.mix";

    @Override
    protected Cache generateNewCache(String cacheName, URL url) {
        String mixCache = CacheConfig.getProperty(MIX_CACHE);
        if(StringUtils.isEmpty(mixCache)){
            throw new IllegalArgumentException("cache.mix must not be null");
        }
        String caches[] = Constants.COMMA_SPLIT_PATTERN.split(mixCache);
        if(caches.length!=2){
            throw new IllegalArgumentException("cache.mix must set two caches,but not set "+caches.length+" ");
        }
        ExtensionLoader cacheFactoryLoader = ExtensionLoader.getExtensionLoader(CacheFactory.class);
        CacheFactory l1CacheFactory  = (CacheFactory) cacheFactoryLoader.getExtension(caches[0]);
        if(l1CacheFactory==null){
            throw new IllegalArgumentException("not found CacheFactory extension by name ["+caches[0]+"]");
        }
        CacheFactory l2CacheFactory = (CacheFactory) cacheFactoryLoader.getExtension(caches[1]);
        if(l2CacheFactory==null){
            throw new IllegalArgumentException("not found CacheFactory extension by name ["+caches[1]+"]");
        }
        return new MixCache(l1CacheFactory.getCache(url),l2CacheFactory.getCache(url),cacheName,url);
    }   
}
