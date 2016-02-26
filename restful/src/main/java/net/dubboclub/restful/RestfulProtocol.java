package net.dubboclub.restful;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.http.HttpBinder;
import com.alibaba.dubbo.remoting.http.HttpServer;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.protocol.AbstractProxyProtocol;
import net.dubboclub.restful.client.RestfulInvoker;
import net.dubboclub.restful.export.RestfulHandler;
import net.dubboclub.restful.export.mapping.ServiceMappingContainer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by bieber on 2015/11/5.
 */
public class RestfulProtocol extends AbstractProxyProtocol{
    
    private static final Map<Class<?>,Object> REFER_MAPPER = new HashMap<Class<?>, Object>();
    
    private static final Map<String,HttpServer> SERVER_MAPPER = new HashMap<String, HttpServer>();

    private static final ServiceMappingContainer SERVICE_MAPPING_CONTAINER = new ServiceMappingContainer();

    private HttpBinder httpBinder;


    @Override
    protected <T> Runnable doExport(T impl, Class<T> type, final URL url) throws RpcException {
        String addr = url.getIp() + ":" + url.getPort();
        HttpServer server = SERVER_MAPPER.get(addr);
        if (server == null) {
            server = httpBinder.bind(url, new RestfulHandler(SERVICE_MAPPING_CONTAINER));
            SERVER_MAPPER.put(addr, server);
        }
        SERVICE_MAPPING_CONTAINER.registerService(url,type,impl);
        return new Runnable() {
            @Override
            public void run() {
                SERVICE_MAPPING_CONTAINER.unregisterService(url);
            }
        };
    }

    @Override
    protected synchronized  <T> T doRefer(Class<T> type, URL url) throws RpcException {
        //do nothing
        return null;
    }

    @Override
    public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
        if(!REFER_MAPPER.containsKey(type)){
            REFER_MAPPER.put(type,new RestfulInvoker(url.setProtocol("http"),type));
        }
        return (Invoker<T>) REFER_MAPPER.get(type);
    }

    @Override
    public int getDefaultPort() {
        return 8080;
    }

    public void setHttpBinder(HttpBinder httpBinder) {
        this.httpBinder = httpBinder;
    }
}
