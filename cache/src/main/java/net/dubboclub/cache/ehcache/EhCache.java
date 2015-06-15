package net.dubboclub.cache.ehcache;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.StringUtils;
import net.dubboclub.cache.AbstractCache;
import net.dubboclub.cache.config.CacheConfig;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;

import java.io.FileNotFoundException;

/**
 * Created by bieber on 2015/5/27.
 */
public class EhCache extends AbstractCache {
    
    private static CacheManager cacheManager;
    
    private static final String CONFIGURATION_FILE="cache.ehcache.configuration";
    
    private Ehcache originCache;
    
    static {
        if(StringUtils.isEmpty(CacheConfig.getProperty(CONFIGURATION_FILE))){
            Configuration configuration = new Configuration();
            CacheConfig.appendProperties(configuration, Ehcache.class);

            CacheConfiguration cacheConfiguration = new CacheConfiguration();
            CacheConfig.appendProperties(cacheConfiguration,EhCache.class);
            if(cacheConfiguration.getMaxEntriesLocalHeap()==0){
                cacheConfiguration.setMaxEntriesLocalHeap(10000);
            }
            if(cacheConfiguration.getMaxEntriesLocalDisk()==0){
                cacheConfiguration.setMaxEntriesLocalDisk(10000000);
            }
            configuration.setDefaultCacheConfiguration(cacheConfiguration);
            cacheManager=CacheManager.create(configuration);
        }else{
            try {
                cacheManager=CacheManager.create(CacheConfig.getConfigurationInputStream(CacheConfig.getProperty(CONFIGURATION_FILE)));
            } catch (FileNotFoundException e) {
                logger.error("Failed to load configuration file",e);
                throw new IllegalArgumentException("Failed to load configuration file",e);
            }
        }
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                cacheManager.shutdown();
            }
        }));
    }

    @Override
    protected String getTagName() {
        return "ehcache";
    }

    public EhCache(String cacheName, URL url) {
        super(cacheName, url);
        originCache = cacheManager.addCacheIfAbsent(cacheName);
    }

    @Override
    public void put(Object key, Object value) {
        if(key==null||value==null){
            return ;
        }
        Element element = new Element(key,value);
        element.setTimeToLive(getExpireSecond(cachedUrl));
        originCache.put(element);
    }

    @Override
    public Object get(Object key) {
        Element element = originCache.get(key);
        if(element==null){
            return null;
        }
        return element.getObjectValue();
    }
}
