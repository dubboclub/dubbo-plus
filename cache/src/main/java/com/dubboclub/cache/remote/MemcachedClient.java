package com.dubboclub.cache.remote;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.dubboclub.cache.config.CacheConfig;
import com.dubboclub.cache.remote.memcached.AdaptiveMemcachedSessionLocator;
import com.google.code.yanf4j.config.Configuration;
import net.rubyeye.xmemcached.MemcachedClientBuilder;
import net.rubyeye.xmemcached.MemcachedClientStateListener;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.exception.MemcachedException;
import net.rubyeye.xmemcached.transcoders.CachedData;
import net.rubyeye.xmemcached.transcoders.CompressionMode;
import net.rubyeye.xmemcached.transcoders.Transcoder;
import net.rubyeye.xmemcached.utils.AddrUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeoutException;

/**
 * Created by bieber on 2015/5/27.
 */
public class MemcachedClient extends RemoteClient{

    private static MemcachedClientBuilder clientBuilder;

    private static volatile net.rubyeye.xmemcached.MemcachedClient client;

    private static final String MEMCACHED_CONNECT="cache.memcached.connect";

    private static final String MEMCACHED_POOL_SIZE="cache.memcached.connect.pool.size";

    private static final String MEMCACHED_CONNECT_TIMEOUT="cache.memcached.conncet.timeout";

    private static final String MEMCACHED_CHECK_SESSION_TIMEOUT_INTERVAL="cache.memcached.check.session.timeout.interval";

    private static final String MEMCACHED_DISPATCH_MESSAGE_THREAD_COUNT="cache.memcached.dispatch.message.thread.count";

    private static final String MEMCACHED_READ_THREAD_COUNT="cache.memcached.read.thread.count";

    private static final String MEMCACHED_WRITE_THREAD_COUNT="cache.memcached.write.thread.count";

    private static final String MEMCACHED_SESSION_IDLE_TIMEOUT="cache.memcached.session.idle.timeout";

    private static final String MEMCACHED_SESSION_READ_BUFFER_SIZE="cache.memcached.session.read.buffer.size";

    private static final String MEMCACHED_SO_TIMEOUT="cache.memcached.so.timeout";

    private static final Logger logger = LoggerFactory.getLogger(MemcachedClient.class);
    static {
        String configConnect= CacheConfig.getProperty(MEMCACHED_CONNECT);
        String[] connects = Constants.COMMA_SPLIT_PATTERN.split(configConnect);
        StringBuffer connectStr = new StringBuffer();
        if(connects.length>0){
            for(String connect:connects){
                connectStr.append(connect).append(" ");
            }
            connectStr.setLength(connectStr.length()-1);
        }else{
            connectStr.append(configConnect);
        }
        clientBuilder =  new XMemcachedClientBuilder(AddrUtil.getAddresses(connectStr.toString()));
        clientBuilder.setConnectionPoolSize(CacheConfig.getProperty(MEMCACHED_POOL_SIZE,net.rubyeye.xmemcached.MemcachedClient.DEFAULT_CONNECTION_POOL_SIZE));
        clientBuilder.setConnectTimeout(CacheConfig.getProperty(MEMCACHED_CONNECT_TIMEOUT, net.rubyeye.xmemcached.MemcachedClient.DEFAULT_CONNECT_TIMEOUT));
        clientBuilder.setSessionLocator(new AdaptiveMemcachedSessionLocator());
        Configuration configuration = new Configuration();
        configuration.setCheckSessionTimeoutInterval(CacheConfig.getProperty(MEMCACHED_CHECK_SESSION_TIMEOUT_INTERVAL, 1000l));
        configuration.setDispatchMessageThreadCount(CacheConfig.getProperty(MEMCACHED_DISPATCH_MESSAGE_THREAD_COUNT, 0));
        configuration.setReadThreadCount(CacheConfig.getProperty(MEMCACHED_READ_THREAD_COUNT, 1));
        configuration.setWriteThreadCount(CacheConfig.getProperty(MEMCACHED_WRITE_THREAD_COUNT, 0));
        configuration.setSessionIdleTimeout(CacheConfig.getProperty(MEMCACHED_SESSION_IDLE_TIMEOUT, 5000l));
        configuration.setSessionReadBufferSize(CacheConfig.getProperty(MEMCACHED_SESSION_READ_BUFFER_SIZE, 32 * 1024));
        configuration.setSoTimeout(CacheConfig.getProperty(MEMCACHED_SO_TIMEOUT, 0));
        clientBuilder.setConfiguration(configuration);
        clientBuilder.addStateListener(new MemcachedClientStateListener() {
            @Override
            public void onStarted(net.rubyeye.xmemcached.MemcachedClient memcachedClient) {
                logger.debug("started memcached client ["+memcachedClient+"]");
            }

            @Override
            public void onShutDown(net.rubyeye.xmemcached.MemcachedClient memcachedClient) {
                logger.debug("shut down memcached client ["+memcachedClient+"]");
            }

            @Override
            public void onConnected(net.rubyeye.xmemcached.MemcachedClient memcachedClient, InetSocketAddress inetSocketAddress) {
                logger.debug("memcached client ["+memcachedClient+"] connected remote ["+inetSocketAddress+"]");
            }

            @Override
            public void onDisconnected(net.rubyeye.xmemcached.MemcachedClient memcachedClient, InetSocketAddress inetSocketAddress) {
                logger.debug("memcached client ["+memcachedClient+"] disconnected remote ["+inetSocketAddress+"]");
            }

            @Override
            public void onException(net.rubyeye.xmemcached.MemcachedClient memcachedClient, Throwable throwable) {
                logger.error("memcache client ["+memcachedClient+"] occur exception" ,throwable);
            }
        });
        buildClient();
    }

    private static void buildClient(){
        if(client==null||client.isShutdown()){
            synchronized (MemcachedClient.client){
                if(client==null||client.isShutdown()){
                    try {
                        client=clientBuilder.build();
                    } catch (IOException e) {
                        throw new IllegalStateException("Failed to build memcached client",e);
                    }
                }
            }
        }
    }

    @Override
    public void cacheValue(byte[] key, byte[] bytes, int expireSecond) {
        if(client.isShutdown()){
            buildClient();
        }
        try {
            client.add(new String(key,"UTF-8"),expireSecond,bytes);
        } catch (TimeoutException e) {
            logger.error("Failed to add cache value by memcached",e);
        } catch (InterruptedException e) {
            logger.error("Failed to add cache value by memcached", e);
        } catch (MemcachedException e) {
            logger.error("Failed to add cache value by memcached",e);
        } catch (UnsupportedEncodingException e) {
            logger.error("Failed to add cache value by memcached", e);
        }
    }

    @Override
    public byte[] getValue(byte[] key) {
        if(client.isShutdown()){
            buildClient();
        }
        try {
            byte[] bytes = client. get(new String(key,"UTF-8"));
            return bytes;
        } catch (TimeoutException e) {
            logger.error("Failed to get cache value from memcached", e);
        } catch (InterruptedException e) {
            logger.error("Failed to get cache value from memcached", e);
        } catch (MemcachedException e) {
            logger.error("Failed to get cache value from memcached", e);
        } catch (UnsupportedEncodingException e) {
            logger.error("Failed to get cache value from memcached", e);
        }
        return null;
    }
}
