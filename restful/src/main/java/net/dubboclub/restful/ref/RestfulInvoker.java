package net.dubboclub.restful.ref;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.protocol.AbstractInvoker;

/**
 * Created by bieber on 2015/11/5.
 */
public class RestfulInvoker<T extends Object> implements Invoker<T> {

    
    private volatile boolean isEnable = true;
    
    private URL url;
    
    private Class<T> type;

    public RestfulInvoker(URL url, Class<T> type) {
        this.url = url;
        this.type = type;
    }

    @Override
    public Class getInterface() {
        return type;
    }

    @Override
    public Result invoke(Invocation invocation) throws RpcException {
        return null;
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public boolean isAvailable() {
        return isEnable;
    }

    @Override
    public void destroy() {
        isEnable=false;
    }
}
