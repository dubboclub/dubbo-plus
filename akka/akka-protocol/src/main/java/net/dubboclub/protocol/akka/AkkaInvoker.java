package net.dubboclub.protocol.akka;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.protocol.AbstractInvoker;
import net.dubboclub.akka.remoting.actor.BasicActor;

/**
 * Created by bieber on 2015/7/8.
 */
public class AkkaInvoker<T> extends AbstractInvoker<T> {
    
    private BasicActor actor;
    
    public AkkaInvoker(Class<T> type, URL url) {
        super(type, url);
    }

    @Override
    protected Result doInvoke(Invocation invocation) throws Throwable {
        return null;
    }
}
