package net.dubboclub.restful;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.http.HttpBinder;
import com.alibaba.dubbo.remoting.http.HttpServer;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.ProxyFactory;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.protocol.AbstractProxyProtocol;
import com.alibaba.dubbo.rpc.support.ProtocolUtils;
import net.dubboclub.restful.export.RestfulHandler;
import net.dubboclub.restful.ref.RestfulInvoker;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bieber on 2015/11/5.
 */
public class RestfulProtocol extends AbstractProxyProtocol{
    
    private static final Map<Class<?>,Object> REFER_MAPPER = new HashMap<Class<?>, Object>();
    
    private static final Map<String,HttpServer> SERVER_MAPPER = new HashMap<String, HttpServer>();

    private HttpBinder httpBinder;

    private ProxyFactory proxyFactory ;

    @Override
    protected <T> Runnable doExport(T impl, Class<T> type, URL url) throws RpcException {
        String addr = url.getIp() + ":" + url.getPort();
        HttpServer server = SERVER_MAPPER.get(addr);
        if (server == null) {
            server = httpBinder.bind(url, new RestfulHandler());
            SERVER_MAPPER.put(addr, server);
        }
        String serviceKey = ProtocolUtils.serviceKey(url);
        Invoker invoker = proxyFactory.getInvoker(impl,type,url);
        return new Runnable() {
            @Override
            public void run() {

            }
        };
    }

    @Override
    protected synchronized  <T> T doRefer(Class<T> type, URL url) throws RpcException {
        if(!REFER_MAPPER.containsKey(type)){
            REFER_MAPPER.put(type,proxyFactory.getProxy(new RestfulInvoker<T>(url,type)));
        }
        return (T) REFER_MAPPER.get(type);
    }

    @Override
    public int getDefaultPort() {
        return 8080;
    }

    public void setHttpBinder(HttpBinder httpBinder) {
        this.httpBinder = httpBinder;
    }

    @Override
    public void setProxyFactory(ProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
    }
}
