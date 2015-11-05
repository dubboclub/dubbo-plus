package net.dubboclub.restful.export;

import com.alibaba.dubbo.rpc.Invoker;

/**
 * Created by bieber on 2015/11/5.
 */
public class ServiceHandler   {

    private Invoker invoker;
    
    private String serviceKey;

    public ServiceHandler(Invoker invoker, String serviceKey) {
        this.invoker = invoker;
        this.serviceKey = serviceKey;
    }
}
