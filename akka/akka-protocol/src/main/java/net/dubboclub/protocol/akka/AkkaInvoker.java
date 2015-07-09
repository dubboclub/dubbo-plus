package net.dubboclub.protocol.akka;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.protocol.AbstractInvoker;
import net.dubboclub.akka.remoting.actor.BasicActor;

/**
 * Created by bieber on 2015/7/8.
 */
public class AkkaInvoker<T> implements Invoker<T> {
    
    private BasicActor actor;

    private URL url;

    private Class<T> type;

    private volatile boolean isAvailable =false;

    public AkkaInvoker(BasicActor actor, URL url,Class<T> type) {
        this.actor = actor;
        this.url = url;
        this.type = type;
        this.isAvailable = true;
    }

    @Override
    public Class<T> getInterface() {
        return type;
    }

    @Override
    public Result invoke(Invocation invocation) throws RpcException {
        if(isAvailable){
            actor.tell(invocation);

        }
        throw new RpcException("Akka invoker for type "+type+" is not available");
    }

    @Override
    public URL getUrl() {
        return url;
    }

    @Override
    public boolean isAvailable() {
        return isAvailable;
    }

    @Override
    public void destroy() {
        isAvailable=false;
        actor.destroy();
    }
}
