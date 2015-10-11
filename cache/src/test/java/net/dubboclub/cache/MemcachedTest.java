package net.dubboclub.cache;

import net.dubboclub.cache.config.CacheConfig;
import net.dubboclub.cache.remote.MemcachedClient;

/**
 * Created by bieber on 2015/9/12.
 */
public class MemcachedTest {

    public static void main(String[] args){
        MemcachedClient client = new MemcachedClient();
        for(int i=0;i<1524*64;i++){
            byte[] key = new String(i+"").getBytes();
            byte[] value = new String("hello world "+i).getBytes();
            client.cacheValue(key,value,600);
            /*byte[] value = client.getValue(key);
            System.out.println(new String(value));*/
        }
        System.out.println("finished");
        System.exit(0);
    }
}
