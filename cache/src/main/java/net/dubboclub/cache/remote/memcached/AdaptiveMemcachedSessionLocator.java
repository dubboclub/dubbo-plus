package net.dubboclub.cache.remote.memcached;

import com.alibaba.dubbo.common.extension.Adaptive;
import net.dubboclub.cache.config.CacheConfig;
import com.google.code.yanf4j.core.Session;
import net.rubyeye.xmemcached.MemcachedSessionLocator;
import net.rubyeye.xmemcached.impl.*;

import java.util.Collection;

/**
 * Created by bieber on 2015/5/27.
 */
@Adaptive
public class AdaptiveMemcachedSessionLocator implements MemcachedSessionLocator {
    private static final String MEMCACHED_SESSION_LOCATOR="cache.memcached.session.locator";

    private static  MemcachedSessionLocator memcachedSessionLocator;

    static {
        memcachedSessionLocator = SessionLocator.getSessionLocator(CacheConfig.getProperty(MEMCACHED_SESSION_LOCATOR, SessionLocator.DEFAULT.value));
    }

    @Override
    public Session getSessionByKey(String s) {
        return memcachedSessionLocator.getSessionByKey(s);
    }

    @Override
    public void updateSessions(Collection<Session> collection) {
        memcachedSessionLocator.updateSessions(collection);
    }

    @Override
    public void setFailureMode(boolean b) {
        memcachedSessionLocator.setFailureMode(b);
    }

    enum SessionLocator{
        KETAMA("ketama",new KetamaMemcachedSessionLocator()),DEFAULT("default",new ArrayMemcachedSessionLocator()),ELECTION("election",new ElectionMemcachedSessionLocator()),LIBMEMCACHED("libmemcached",new LibmemcachedMemcachedSessionLocator()),PHP("php",new PHPMemcacheSessionLocator());
        private String value;
        private MemcachedSessionLocator memcachedSessionLocator;
        SessionLocator(String value,MemcachedSessionLocator memcachedSessionLocator){
            this.value=value;
            this.memcachedSessionLocator=memcachedSessionLocator;
        }
        public static MemcachedSessionLocator getSessionLocator(String value){
            SessionLocator[] sessionLocators = SessionLocator.values();
            for(SessionLocator sessionLocator:sessionLocators){
                if(sessionLocator.value.equalsIgnoreCase(value)){
                    return sessionLocator.memcachedSessionLocator;
                }
            }
            return DEFAULT.memcachedSessionLocator;
        }
    }
}
