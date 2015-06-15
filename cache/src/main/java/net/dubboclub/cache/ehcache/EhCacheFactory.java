package net.dubboclub.cache.ehcache;

import com.alibaba.dubbo.cache.Cache;
import com.alibaba.dubbo.common.URL;
import net.dubboclub.cache.AbstractCacheFactory;

/**
 * Created by bieber on 2015/5/27.
 */
public class EhCacheFactory extends AbstractCacheFactory {

    @Override
    protected Cache generateNewCache(String cacheName, URL url) {
        return new EhCache(cacheName,url);
    }
}
