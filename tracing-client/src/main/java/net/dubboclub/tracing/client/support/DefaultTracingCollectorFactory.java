package net.dubboclub.tracing.client.support;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Protocol;
import com.alibaba.dubbo.rpc.ProxyFactory;
import net.dubboclub.tracing.api.TracingCollector;

/**
 * DefaultTracingCollectorFactory
 * Created by bieber.bibo on 16/7/18
 */

public class DefaultTracingCollectorFactory extends AbstractTracingCollectorFactory {

    private Protocol protocol;

    private ProxyFactory proxyFactory;

    @Override
    protected TracingCollector createTracingCollector(URL url) {
        Invoker<TracingCollector> invoker = protocol.refer(TracingCollector.class,url);
        TracingCollector tracingCollector = proxyFactory.getProxy(invoker);
        return tracingCollector;
    }


    public void setProxyFactory(ProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }
}
